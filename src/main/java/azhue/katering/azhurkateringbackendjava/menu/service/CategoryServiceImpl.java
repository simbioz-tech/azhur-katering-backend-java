package azhue.katering.azhurkateringbackendjava.menu.service;

import azhue.katering.azhurkateringbackendjava.menu.exception.category.CategoryAlreadyExistsException;
import azhue.katering.azhurkateringbackendjava.menu.exception.category.CategoryHasDishesException;
import azhue.katering.azhurkateringbackendjava.menu.exception.category.CategoryNotFoundException;
import azhue.katering.azhurkateringbackendjava.menu.model.dto.request.CategoryRequest;
import azhue.katering.azhurkateringbackendjava.menu.model.dto.response.CategoryResponse;
import azhue.katering.azhurkateringbackendjava.menu.model.entity.Category;
import azhue.katering.azhurkateringbackendjava.menu.repository.CategoryRepository;
import azhue.katering.azhurkateringbackendjava.menu.repository.DishRepository;
import azhue.katering.azhurkateringbackendjava.menu.service.contract.CategoryService;
import azhue.katering.azhurkateringbackendjava.menu.service.mapper.CategoryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Реализация сервиса для работы с категориями
 *
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CategoryServiceImpl implements CategoryService {
    
    private final CategoryRepository categoryRepository;
    private final DishRepository dishRepository;
    private final CategoryMapper categoryMapper;
    
    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        log.info("Получение всех категорий");
        List<Category> categories = categoryRepository.findAll();

        return categoryMapper.toResponseList(categories);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getActiveCategories() {
        log.info("Получение активных категорий");
        List<Category> categories = categoryRepository.findByIsActiveTrue();
        return categoryMapper.toResponseList(categories);
    }
    
    @Override
    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        log.info("Создание новой категории: {}", request.getName());
        
        // Проверяем, что категория с таким названием не существует
        if (categoryRepository.existsByName(request.getName())) {
            log.warn("Категория с таким именем уже есть: {}", request.getName());
            throw new CategoryAlreadyExistsException("Категория с таким названием существует");
        }
        
        Category category = Category.builder()
                .name(request.getName())
                .isActive(false)
                .build();
        
        Category savedCategory = categoryRepository.save(category);
        log.info("Создана новая категория: {} (ID: {})", request.getName(), savedCategory.getId());
        
        return categoryMapper.toResponse(savedCategory);
    }
    
    @Override
    @Transactional
    public CategoryResponse updateCategory(UUID id, CategoryRequest request) {
        log.info("Обновление категории с ID: {}", id);
        
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Категория с id: {} не найдено", id);
                    return new CategoryNotFoundException("Категория не найдена");
                });
        
        // Проверяем, что новое название не конфликтует с существующими
        if (!category.getName().equals(request.getName()) && 
            categoryRepository.existsByNameAndIdNot(request.getName(), id)) {
            log.warn("Категория с таким именем уже есть: {}, id: {}", request.getName(), id);
            throw new CategoryAlreadyExistsException("Категория с таким названием существует");
        }
        
        category.setName(request.getName());
        if (request.getIsActive() != null) {
            category.setIsActive(request.getIsActive());
        }
        
        Category updatedCategory = categoryRepository.save(category);
        log.info("Обновлена категория: {} (ID: {})", request.getName(), id);
        
        return categoryMapper.toResponse(updatedCategory);
    }
    
    @Override
    @Transactional
    public void deleteCategory(UUID id) {
        log.info("Удаление категории с ID: {}", id);
        
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Категория с id: {} не найдено", id);
                    return new CategoryNotFoundException("Категория не найдена");
                });
        
        // Проверяем, что в категории нет блюд
        if (dishRepository.existsByCategoryId(id)) {
            throw new CategoryHasDishesException("В категории есть блюда");
        }
        
        categoryRepository.delete(category);
        log.info("Удалена категория: {} (ID: {})", category.getName(), id);
    }
    
    @Override
    @Transactional
    public void toggleCategoryStatus(UUID id) {
        log.info("Изменение статуса категории с ID: {}", id);
        
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Категория с id: {} не найдено", id);
                    return new CategoryNotFoundException("Категория не найдена");
                });
        
        category.setIsActive(!category.getIsActive());
        Category updatedCategory = categoryRepository.save(category);
        
        String status = updatedCategory.getIsActive() ? "активирована" : "деактивирована";
        log.info("Категория {} {} (ID: {})", category.getName(), status, id);
    }
}
