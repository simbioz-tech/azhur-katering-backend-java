package azhue.katering.azhurkateringbackendjava.menu.controller;

import azhue.katering.azhurkateringbackendjava.menu.model.dto.request.CategoryRequest;
import azhue.katering.azhurkateringbackendjava.menu.model.dto.response.CategoryResponse;
import azhue.katering.azhurkateringbackendjava.menu.service.contract.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Контроллер для управления категориями
 * Публичные методы доступны без авторизации
 * Админские методы требуют роль ADMIN
 *
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Tag(name = "Категории", description = "API для работы с категориями")
public class CategoryController {
    
    private final CategoryService categoryService;

    @GetMapping
    @Operation(
            summary = "Получить все активные категории",
            description = "Публичный метод для получения списка всех активных категорий"
    )
    public List<CategoryResponse> getActiveCategories() {

        return categoryService.getActiveCategories();
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    @GetMapping("/all")
    @Operation(
            summary = "Получить все категории",
            description = "Метод для получения списка всех категорий (требует роль ADMIN или MODERATOR)"
    )
    public List<CategoryResponse> getAllCategories() {

        return categoryService.getAllCategories();
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    @PostMapping
    @Operation(
            summary = "Создать категорию",
            description = "Создание новой категории (требует роль ADMIN или MODERATOR)"
    )
    public CategoryResponse createCategory(@Valid @RequestBody CategoryRequest categoryRequest) {

        return categoryService.createCategory(categoryRequest);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    @PutMapping("/{id}")
    @Operation(
            summary = "Обновить категорию",
            description = "Обновление категории по ID (требует роль ADMIN или MODERATOR)"
    )
    public CategoryResponse updateCategory(
            @Parameter(description = "ID категории") @PathVariable UUID id,
            @Valid @RequestBody CategoryRequest categoryRequest) {

        return categoryService.updateCategory(id, categoryRequest);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    @DeleteMapping("/{id}")
    @Operation(
            summary = "Удалить категорию",
            description = "Удаление категории по ID (требует роль ADMIN или MODERATOR)"
    )
    public ResponseEntity<Void> deleteCategory(
            @Parameter(description = "ID категории") @PathVariable UUID id) {

        categoryService.deleteCategory(id);

        return ResponseEntity.noContent().build();
    }
}
