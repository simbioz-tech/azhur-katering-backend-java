package azhue.katering.azhurkateringbackendjava.menu.controller;

import azhue.katering.azhurkateringbackendjava.common.model.dto.PaginatedResponse;
import azhue.katering.azhurkateringbackendjava.common.service.MetricsService;
import azhue.katering.azhurkateringbackendjava.menu.model.dto.request.DishRequest;
import azhue.katering.azhurkateringbackendjava.menu.model.dto.response.DishResponse;
import azhue.katering.azhurkateringbackendjava.menu.service.contract.DishService;
import io.micrometer.core.instrument.Timer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * 
 * Контроллер для работы с блюдами
 * 
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/dishes")
@RequiredArgsConstructor
@Tag(name = "Блюда", description = "API для работы с блюдами")
public class DishController {

    public static final int MAX_PAGE_SIZE = 100;
    
    private final DishService dishService;
    private final MetricsService metricsService;

    @GetMapping("/available")
    @Operation(
            summary = "Получить все доступные блюда",
            description = "Публичный метод для получения всех доступных блюд. " +
                         "Данные кэшируются на уровне сервиса на 15 минут. " +
                         "Frontend должен фильтровать блюда по категориям на клиенте."
    )
    public ResponseEntity<List<DishResponse>> getAllAvailableDishes() {

        Timer.Sample timer = metricsService.startDishReadProcessingTimer();
        
        try {
            List<DishResponse> dishes = dishService.getAvailableDishes();
            metricsService.incrementDishRead();
            
            log.info("Успешно получено {} доступных блюд", dishes.size());
            return ResponseEntity.ok(dishes);
        } finally {
            metricsService.stopDishReadProcessingTimer(timer);
        }
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Получить блюдо по ID",
            description = "Публичный метод для получения детальной информации о блюде"
    )
    public ResponseEntity<DishResponse> getDish(
            @Parameter(description = "ID блюда") @PathVariable UUID id) {

        Timer.Sample timer = metricsService.startDishReadProcessingTimer();
        
        try {
            DishResponse dish = dishService.getDishById(id);
            metricsService.incrementDishRead();
            
            log.info("Успешно получено блюдо: {} (ID: {})", dish.getName(), id);
            return ResponseEntity.ok(dish);
        } finally {
            metricsService.stopDishReadProcessingTimer(timer);
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    @GetMapping
    @Operation(
            summary = "Получить все блюда с пагинацией",
            description = "Получение списка всех блюд с пагинацией для административного управления " +
                         "(требует роль ADMIN или MODERATOR)"
    )
    public ResponseEntity<PaginatedResponse<DishResponse>> getAllDishes(
            @Parameter(description = "Номер страницы (начиная с 0)") 
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Размер страницы (максимум 100)") 
            @RequestParam(defaultValue = "20") int size) {

        Timer.Sample timer = metricsService.startDishReadProcessingTimer();

        try {
            size = Math.min(size, MAX_PAGE_SIZE);

            Pageable pageable = PageRequest.of(page, size);
            Page<DishResponse> dishes = dishService.getAllDishes(pageable);
            metricsService.incrementDishRead();

            log.info("Успешно получено {} блюд (страница {}, размер {})",
                    dishes.getContent().size(), page, size);

            return ResponseEntity.ok(PaginatedResponse.fromPage(dishes));
        } finally {
            metricsService.stopDishReadProcessingTimer(timer);
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    @GetMapping("/category/{category_id}")
    public ResponseEntity<PaginatedResponse<DishResponse>> getDishByCategory(
            @Parameter(description = "ID категории") @PathVariable UUID category_id,
            @Parameter(description = "Номер страницы (начиная с 0)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Размер страницы (максимум 100)")
            @RequestParam(defaultValue = "20") int size){

        size = Math.min(size, MAX_PAGE_SIZE);

        Pageable pageable = PageRequest.of(page, size);
        Page<DishResponse> dishResponses = dishService.getDishesByCategory(category_id, pageable);

        return ResponseEntity.ok(PaginatedResponse.fromPage(dishResponses));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    @GetMapping("/filter")
    public ResponseEntity<PaginatedResponse<DishResponse>> getDishByFilter(
            @Parameter(description = "ID категории") @RequestParam(required = false) UUID category_id,
            @Parameter(description = "Доступность") @RequestParam(required = false) Boolean is_available,
            @Parameter(description = "Номер страницы (начиная с 0)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Размер страницы (максимум 100)")
            @RequestParam(defaultValue = "20") int size){

        size = Math.min(size, MAX_PAGE_SIZE);

        log.info("TEST" + is_available);

        Pageable pageable = PageRequest.of(page, size);
        Page<DishResponse> dishResponses = dishService.searchDishes(category_id, is_available, pageable);

        return ResponseEntity.ok(PaginatedResponse.fromPage(dishResponses));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    @PostMapping
    @Operation(
            summary = "Создать блюдо",
            description = "Создание нового блюда с возможностью загрузки изображения " +
                         "(требует роль ADMIN или MODERATOR). " +
                    "Кэш доступных блюд автоматически обновляется на уровне сервиса."
    )
    public ResponseEntity<DishResponse> createDish(
            @Valid @RequestPart("dish") DishRequest dishRequest,
            @RequestPart(value = "image", required = false) MultipartFile image) throws IOException {

        Timer.Sample timer = metricsService.startDishCreateProcessingTimer();
        
        try {
            DishResponse createdDish = dishService.createDish(dishRequest, image);
            metricsService.incrementDishCreate();
            
            log.info("Успешно создано блюдо: {} (ID: {})", createdDish.getName(), createdDish.getId());
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(createdDish);
        } finally {
            metricsService.stopDishCreateProcessingTimer(timer);
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    @PutMapping("/{id}")
    @Operation(
            summary = "Обновить блюдо",
            description = "Обновление блюда по ID с возможностью загрузки нового изображения " +
                         "(требует роль ADMIN или MODERATOR)." +
                    " Кэш доступных блюд автоматически обновляется на уровне сервиса."
    )
    public ResponseEntity<DishResponse> updateDish(
            @Parameter(description = "ID блюда") @PathVariable UUID id,
            @Valid @RequestPart("dish") DishRequest dishRequest,
            @RequestPart(value = "image", required = false) MultipartFile image) throws IOException {

        Timer.Sample timer = metricsService.startDishUpdateProcessingTimer();
        
        try {
            DishResponse updatedDish = dishService.updateDish(id, dishRequest, image);
            metricsService.incrementDishUpdate();
            
            log.info("Успешно обновлено блюдо: {} (ID: {})", updatedDish.getName(), id);
            return ResponseEntity.ok(updatedDish);
        } finally {
            metricsService.stopDishUpdateProcessingTimer(timer);
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @Operation(
            summary = "Удалить блюдо",
            description = "Удаление блюда по ID (требует роль ADMIN). " +
                    "Кэш доступных блюд автоматически обновляется на уровне сервиса."
    )
    public ResponseEntity<Void> deleteDish(
            @Parameter(description = "ID блюда") @PathVariable UUID id) {

        Timer.Sample timer = metricsService.startDishDeleteProcessingTimer();
        
        try {
            dishService.deleteDish(id);
            metricsService.incrementDishDelete();
            
            log.info("Успешно удалено блюдо с ID: {}", id);
            return ResponseEntity.noContent().build();
        } finally {
            metricsService.stopDishDeleteProcessingTimer(timer);
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    @PatchMapping("/{id}/availability")
    @Operation(
            summary = "Изменить доступность блюда",
            description = "Переключение статуса доступности блюда " +
                         "(требует роль ADMIN или MODERATOR). " +
                    "Кэш доступных блюд автоматически обновляется на уровне сервиса."
    )
    public ResponseEntity<DishResponse> toggleDishAvailability(
            @Parameter(description = "ID блюда") @PathVariable UUID id) {

        Timer.Sample timer = metricsService.startDishUpdateProcessingTimer();
        
        try {
            DishResponse updatedDish = dishService.toggleDishAvailability(id);
            metricsService.incrementDishAvailabilityToggle();
            
            String status = updatedDish.getIsAvailable() ? "доступно" : "недоступно";
            log.info("Успешно изменена доступность блюда: {} теперь {} (ID: {})",
                    updatedDish.getName(), status, id);
            
            return ResponseEntity.ok(updatedDish);
        } finally {
            metricsService.stopDishUpdateProcessingTimer(timer);
        }
    }
}
