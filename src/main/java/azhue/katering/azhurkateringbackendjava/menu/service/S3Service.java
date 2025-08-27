package azhue.katering.azhurkateringbackendjava.menu.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;

/**
 * Сервис для работы с S3-совместимым хранилищем VK Cloud
 * Обеспечивает загрузку, удаление и получение URL изображений
 *
 * @version 1.0.0
 */
@Slf4j
@Service
@Getter
@RequiredArgsConstructor
public class S3Service {
    
    private final AmazonS3 s3Client;

    @Value("${app.s3.bucket}")
    private String bucketName;

    @Value("${app.s3.cache-control}")
    private String cacheControl;

    @Value("${app.s3.endpoint}")
    private String endpoint;
    
    /**
     * Загружает изображение в S3
     */
    public String uploadImage(UUID dishId, MultipartFile file) throws IOException {

        log.info("Загрузка изображения в S3: dishId={}, filename={}, size={}KB",
                dishId, file.getOriginalFilename(), file.getSize() / 1024);

        String key = "dishes/" + dishId + "/original." + getFileExtension(file.getOriginalFilename());

        return upload(file, key);
    }

    private String upload(MultipartFile file, String key) throws IOException {

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(file.getContentType());
        metadata.setContentLength(file.getSize());
        metadata.setCacheControl(cacheControl);


        // Создаем PutObjectRequest с публичным ACL
        PutObjectRequest putRequest = new PutObjectRequest(bucketName, key, file.getInputStream(), metadata)
                .withCannedAcl(CannedAccessControlList.PublicRead);

        s3Client.putObject(putRequest);

        String url = getS3Url(key);

        log.info("Изображение загружено в S3 с публичным доступом: {}", url);
        return url;
    }

    /**
     * Получает расширение файла
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf('.') == -1) {
            return "jpg";
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }

    public String uploadThumbnail(UUID dishId, MultipartFile file) throws IOException {
        log.info("Загрузка сжатого изображения в S3: dishId={}, filename={}, size={}KB",
                dishId, file.getOriginalFilename(), file.getSize() / 1024);

        String key = "dishes/" + dishId + "/thumbnail." + getFileExtension(file.getOriginalFilename());

        // Сжимаем БЕЗ изменения размера (как на photo-editor.ai)
        return uploadCompressedImage(file, key);
    }

    /**
     * Загружает сжатое изображение в S3 с заданным качеством БЕЗ изменения размера
     */
    private String uploadCompressedImage(MultipartFile originalFile, String key) throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            double quality = 0.8;

            // Сжимаем изображение БЕЗ изменения размера
            Thumbnails.of(originalFile.getInputStream())
                    .scale(1.0)  // Сохраняем оригинальный размер
                    .outputQuality(quality)
                    .outputFormat("jpg")  // Принудительно JPEG для лучшего сжатия
                    .toOutputStream(outputStream);

            byte[] compressedBytes = outputStream.toByteArray();

            log.info("Изображение сжато без изменения размера: оригинал={}KB, сжато={}KB, качество={}%, экономия={}%",
                    originalFile.getSize() / 1024,
                    compressedBytes.length / 1024,
                    (int)(quality * 100),
                    (int)((1.0 - (double)compressedBytes.length / originalFile.getSize()) * 100));

            // Создаем метаданные для сжатого изображения
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType("image/jpeg");
            metadata.setContentLength(compressedBytes.length);
            metadata.setCacheControl(cacheControl);

            // Загружаем сжатое изображение в S3
            PutObjectRequest putRequest = new PutObjectRequest(bucketName, key,
                    new ByteArrayInputStream(compressedBytes), metadata)
                    .withCannedAcl(CannedAccessControlList.PublicRead);

            s3Client.putObject(putRequest);

            String url = getS3Url(key);
            log.info("Сжатое изображение загружено в S3: {}", url);
            return url;
        }
    }

    /**
     * Получает полный URL для ключа S3
     */
    private String getS3Url(String key) {
        return s3Client.getUrl(bucketName, key).toString();
    }

    /**
     * Удаляет изображение блюда из S3
     */
    public void deleteImage(UUID dishId) {
        log.info("Удаление изображения из S3: dishId={}", dishId);


        // Удаляем все файлы для блюда
        ListObjectsV2Request request = new ListObjectsV2Request()
                .withBucketName(bucketName)
                .withPrefix("dishes/" + dishId + "/");

        ListObjectsV2Result result = s3Client.listObjectsV2(request);
        int deletedCount = 0;

        for (S3ObjectSummary object : result.getObjectSummaries()) {
            s3Client.deleteObject(bucketName, object.getKey());
            deletedCount++;
            log.debug("Удален файл из S3: {}", object.getKey());
        }

        log.info("Удалено {} файлов из S3 для блюда: {}", deletedCount, dishId);
    }
}
