package azhue.katering.azhurkateringbackendjava.common.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class S3Config {

    @Value("${app.s3.bucket}")
    private String bucketName;

    @Value("${app.s3.cache-control}")
    private String cacheControl;

    @Value("${app.s3.endpoint}")
    private String endpoint;

    @Value("${app.s3.region}")
    private String region;

    @Value("${app.s3.access-key}")
    private String accessKey;

    @Value("${app.s3.secret-key}")
    private String secretKey;

    @Bean
    public AmazonS3 s3Client() {

        log.info("Инициализация S3 клиента для VK Cloud: bucket={}, endpoint={}, region={}",
                bucketName, endpoint, region);

        // Настройка S3 клиента для VK Cloud
        return AmazonS3ClientBuilder.standard()
                .withEndpointConfiguration(
                        new AwsClientBuilder.EndpointConfiguration(endpoint, region)
                )
                .withCredentials(new AWSStaticCredentialsProvider(
                        new BasicAWSCredentials(accessKey, secretKey)
                ))
                .withPathStyleAccessEnabled(true)
                .withChunkedEncodingDisabled(false)
                .build();
    }
}
