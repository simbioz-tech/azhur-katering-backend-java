package azhue.katering.azhurkateringbackendjava.menu.service;

import azhue.katering.azhurkateringbackendjava.common.service.MetricsService;
import azhue.katering.azhurkateringbackendjava.menu.exception.category.CategoryNotFoundException;
import azhue.katering.azhurkateringbackendjava.menu.exception.dish.DishNotFoundException;
import azhue.katering.azhurkateringbackendjava.menu.exception.image.ImageEmptyException;
import azhue.katering.azhurkateringbackendjava.menu.exception.image.ImageExtensionException;
import azhue.katering.azhurkateringbackendjava.menu.exception.image.ImageSizeException;
import azhue.katering.azhurkateringbackendjava.menu.exception.image.ImageTypeException;
import azhue.katering.azhurkateringbackendjava.menu.model.dto.request.DishRequest;
import azhue.katering.azhurkateringbackendjava.menu.model.dto.response.DishResponse;
import azhue.katering.azhurkateringbackendjava.menu.model.entity.Category;
import azhue.katering.azhurkateringbackendjava.menu.model.entity.Dish;
import azhue.katering.azhurkateringbackendjava.menu.repository.CategoryRepository;
import azhue.katering.azhurkateringbackendjava.menu.repository.DishRepository;
import azhue.katering.azhurkateringbackendjava.menu.service.contract.DishService;
import azhue.katering.azhurkateringbackendjava.menu.service.contract.CategoryService;
import azhue.katering.azhurkateringbackendjava.menu.service.mapper.DishMapper;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * Реализация сервиса для работы с блюдами
 * Интегрирован с S3 хранилищем для изображений
 *
 * @version 1.0.0
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class DishServiceImpl implements DishService {

    @Value("${app.images.max.file.size:5242880}") // 5MB
    private long maxFileSize;
    
    private final DishRepository dishRepository;
    private final CategoryRepository categoryRepository;
    private final CategoryService categoryService;
    private final DishMapper dishMapper;
    private final MetricsService metricsService;
    private final S3Service s3Service;

    @Override
    @Transactional(readOnly = true)
    public Page<DishResponse> getAllDishes(Pageable pageable) {
        log.debug("Получение всех блюд с пагинацией: страница={}, размер={}",
                pageable.getPageNumber(), pageable.getPageSize());
        
        Page<Dish> dishes = dishRepository.findAll(pageable);
        return dishes.map(dishMapper::toResponse);
    }
    
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "available-dishes", key = "'all'")
    public List<DishResponse> getAvailableDishes() {
        log.debug("Получение всех доступных блюд");
        
        List<Dish> dishes = dishRepository.findByIsAvailableTrue();
        return dishMapper.toResponseList(dishes);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DishResponse> getDishesByCategory(UUID categoryId, Pageable pageable) {
        log.debug("Получение блюд по категории: {}", categoryId);

        Page<Dish> dishes = dishRepository.findByCategoryId(categoryId, pageable);

        return dishes.map(dishMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DishResponse> searchDishes(UUID categoryId, Boolean isAvailable, Pageable pageable) {
        log.info("Поиск блюд с фильтрами: categoryId={}, isAvailable={}, page={}, size={}",
                categoryId, isAvailable, pageable.getPageNumber(), pageable.getPageSize());
        Timer.Sample timer = metricsService.startDishSearchProcessingTimer();

        try {
            Page<Dish> dishes = dishRepository.findByFilters(categoryId, isAvailable, pageable);

            metricsService.incrementDishSearch();
            log.debug("Найдено {} блюд на странице {} из {}",
                    dishes.getContent().size(), dishes.getNumber(), dishes.getTotalPages());

            return dishes.map(dishMapper::toResponse);
        } finally {
            metricsService.stopDishSearchProcessingTimer(timer);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public DishResponse getDishById(UUID id) {
        log.info("Поиск блюда по ID: {}", id);
        
        Dish dish = dishRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Блюдо с id: {} не найдено", id);
                    return new DishNotFoundException("Блюдо не найдено");
                });
        
        return dishMapper.toResponse(dish);
    }
    
    @Override
    @Transactional
    @CacheEvict(value = "available-dishes", allEntries = true)
    public DishResponse createDish(DishRequest request, MultipartFile file) throws IOException {
        log.info("Создание нового блюда: {} (изображение: {})",
                request.getName(), file != null ? "предоставлено" : "не предоставлено");
        Timer.Sample timer = metricsService.startDishCreateProcessingTimer();
        
        try {
            // Поиск категории
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> {
                        log.warn("Категория с id: {} не найдено", request.getCategoryId());
                        return new CategoryNotFoundException("Категория не найдена");
                    });
            
            log.debug("Категория найдена: {} (ID: {})", category.getName(), category.getId());
            
            // Создание блюда
            Dish dish = Dish.builder()
                    .name(request.getName())
                    .description(request.getDescription())
                    .price(request.getPrice())
                    .category(category)
                    .isAvailable(request.getIsAvailable() != null ? request.getIsAvailable() : true)
                    .build();
            
            Dish savedDish = dishRepository.save(dish);
            log.info("Блюдо сохранено в БД: {} (ID: {})", savedDish.getName(), savedDish.getId());

            // Загрузка изображения в S3
            if (file != null) {

                validateImageFile(file);

                savedDish.setImageUrl(uploadImageToS3(savedDish.getId(), file));
                savedDish.setThumbnailUrl(s3Service.uploadThumbnail(savedDish.getId(), file));
            } else {
                log.info("Изображение не предоставлено для блюда: {}", savedDish.getId());
            }

            savedDish = dishRepository.save(savedDish);
            log.info("Блюдо обновлено с путем к изображению");

            if(!category.getIsActive()) {
                categoryService.toggleCategoryStatus(category.getId());
                log.info("Статус категории обновлен: {} (ID: {})", category.getName(), category.getId());
            }

            metricsService.incrementDishCreate();
            log.info("Успешно создано блюдо: {} (ID: {})", savedDish.getName(), savedDish.getId());
            
            return dishMapper.toResponse(savedDish);
        } finally {
            metricsService.stopDishCreateProcessingTimer(timer);
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf('.') == -1) {
            return "jpg"; // По умолчанию
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }

    private String uploadImageToS3(UUID dishId, MultipartFile file) throws IOException {

        Timer.Sample timer = metricsService.startImageUploadProcessingTimer();

        try {
            String s3Url = s3Service.uploadImage(dishId, file);
            metricsService.incrementImageUpload();

            return s3Url;

        } finally {
            metricsService.stopImageUploadProcessingTimer(timer);
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = "available-dishes", allEntries = true)
    public DishResponse updateDish(UUID id, DishRequest request, MultipartFile file) throws IOException {

        log.info("Обновление блюда с ID: {} (изображение: {})",
                id, file != null ? "предоставлено" : "не предоставлено");
        Timer.Sample timer = metricsService.startDishUpdateProcessingTimer();
        
        try {
            Dish dish = dishRepository.findById(id)
                    .orElseThrow(() -> {
                        log.warn("Блюдо с id: {} не найдено", id);
                        return new DishNotFoundException("Блюдо не найдена");
                    });

            // Обновляем поля, если они предоставлены
            if (file != null) {
                validateImageFile(file);
                String newImageUrl = updateImageInS3(id, file);

                dish.setImageUrl(newImageUrl);
                dish.setThumbnailUrl(s3Service.uploadThumbnail(dish.getId(), file));
            }
            if (request.getName() != null) {
                log.info("Обновление названия: '{}' -> '{}'", dish.getName(), request.getName());
                dish.setName(request.getName());
            }
            if (request.getDescription() != null) {
                log.info("Обновление описания для блюда: {}", id);
                dish.setDescription(request.getDescription());
            }
            if (request.getPrice() != null) {
                log.info("Обновление цены: {} -> {}", dish.getPrice(), request.getPrice());
                dish.setPrice(request.getPrice());
            }
            if (request.getCategoryId() != null) {

                Category category = categoryRepository.findById(request.getCategoryId())
                        .orElseThrow(() -> {
                            log.warn("Категория с id: {} не найдено", request.getCategoryId());
                            return new CategoryNotFoundException("Категория не найдена");
                        });

                log.info("Обновление категории: {} -> {}",
                        dish.getCategory().getName(), category.getName());
                dish.setCategory(category);
            }
            if (request.getIsAvailable() != null) {
                log.info("Обновление доступности: {} -> {}", dish.getIsAvailable(), request.getIsAvailable());
                dish.setIsAvailable(request.getIsAvailable());
            }
            
            Dish updatedDish = dishRepository.save(dish);
            metricsService.incrementDishUpdate();
            
            log.info("Успешно обновлено блюдо: {} (ID: {})", updatedDish.getName(), id);
            
            return dishMapper.toResponse(updatedDish);
        } finally {
            metricsService.stopDishUpdateProcessingTimer(timer);
        }
    }



    private void validateImageFile(MultipartFile file) {

        log.debug("Валидация файла: размер={} байт, тип={}, имя={}",
                file.getSize(), file.getContentType(), file.getOriginalFilename());

        if (file.isEmpty()) {
            log.warn("Файл пустой");
            throw new ImageEmptyException("Файл пустой");
        }

        if (file.getSize() > maxFileSize) {
            log.warn("Размер файла превышает максимально допустимый: {} MB", maxFileSize / 1024 / 1024);
            throw new ImageSizeException("Размер файла превышает максимально допустимый: " +
                    (maxFileSize / 1024 / 1024) + "MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            log.warn("Файл не является изображением");
            throw new ImageTypeException("Файл не является изображением. Поддерживаемые типы: image/jpeg, image/png, image/webp");
        }

        // Проверка расширения файла
        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null) {
            String extension = getFileExtension(originalFilename);
            if (!isValidImageExtension(extension)) {
                log.warn("Неподдерживаемый формат изображения");
                throw new ImageExtensionException("Неподдерживаемый формат изображения. Поддерживаемые форматы: jpg, jpeg, png, webp");
            }
        }

        log.info("Файл прошел валидацию");
    }

    private boolean isValidImageExtension(String extension) {
        return extension.equals("jpg") ||
                extension.equals("jpeg") ||
                extension.equals("png") ||
                extension.equals("webp");
    }

    private String updateImageInS3(UUID dishId, MultipartFile file) throws IOException {

        Timer.Sample timer = metricsService.startImageUpdateProcessingTimer();

        try {
            // Удаляем старое изображение из S3
            log.info("Удаление старого изображения из S3 для блюда: {}", dishId);
            s3Service.deleteImage(dishId);

            // Загружаем новое изображение в S3
            log.info("Загрузка нового изображения в S3 для блюда: {}", dishId);
            String newImageUrl = uploadImageToS3(dishId, file);

            metricsService.incrementImageUpdate();

            return newImageUrl;
        } finally {
            metricsService.stopImageUpdateProcessingTimer(timer);
        }
    }
    
    @Override
    @Transactional
    @CacheEvict(value = "available-dishes", allEntries = true)
    public void deleteDish(UUID id) {

        log.info("Удаление блюда с ID: {}", id);
        Timer.Sample timer = metricsService.startDishDeleteProcessingTimer();
        
        try {
            Dish dish = dishRepository.findById(id)
                    .orElseThrow(() -> {
                        log.warn("Блюдо с id: {} не найдено", id);
                        return new DishNotFoundException("Блюдо не найдена");
                    });
            
            // Удаляем изображение из S3
            if (dish.getImageUrl() != null) {
                s3Service.deleteImage(id);
            }
            
            dishRepository.delete(dish);
            metricsService.incrementDishDelete();
            
            log.info("Успешно удалено блюдо: {} (ID: {})", dish.getName(), id);
        } finally {
            metricsService.stopDishDeleteProcessingTimer(timer);
        }
    }
    
    @Override
    @Transactional
    @CacheEvict(value = "available-dishes", allEntries = true)
    public DishResponse toggleDishAvailability(UUID id) {

        log.info("Изменение статуса доступности блюда с ID: {}", id);
        Timer.Sample timer = metricsService.startDishUpdateProcessingTimer();
        
        try {
            Dish dish = dishRepository.findById(id)
                    .orElseThrow(() -> {
                        log.warn("Блюдо с id: {} не найдено", id);
                        return new DishNotFoundException("Блюдо не найдена");
                    });
            
            boolean newStatus = !dish.getIsAvailable();
            dish.setIsAvailable(newStatus);
            
            Dish updatedDish = dishRepository.save(dish);
            metricsService.incrementDishAvailabilityToggle();
            
            log.info("Статус доступности изменен: {} -> {} (ID: {})",
                    !newStatus, newStatus, id);
            
            return dishMapper.toResponse(updatedDish);
        } finally {
            metricsService.stopDishUpdateProcessingTimer(timer);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<DishResponse> searchDishesByName(String name) {
        log.debug("Поиск блюд по названию: '{}'", name);
        Timer.Sample timer = metricsService.startDishSearchProcessingTimer();

        try {
            List<Dish> dishes = dishRepository.findByNameContainingIgnoreCase(name);
            metricsService.incrementDishSearch();

            log.debug("Найдено {} блюд по запросу '{}'", dishes.size(), name);
            return dishMapper.toResponseList(dishes);
        } finally {
            metricsService.stopDishSearchProcessingTimer(timer);
        }
    }


}
