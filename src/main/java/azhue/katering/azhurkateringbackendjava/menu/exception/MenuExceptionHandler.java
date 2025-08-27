package azhue.katering.azhurkateringbackendjava.menu.exception;

import azhue.katering.azhurkateringbackendjava.common.model.dto.ApiResponse;
import azhue.katering.azhurkateringbackendjava.menu.exception.category.CategoryAlreadyExistsException;
import azhue.katering.azhurkateringbackendjava.menu.exception.category.CategoryHasDishesException;
import azhue.katering.azhurkateringbackendjava.menu.exception.category.CategoryNotFoundException;
import azhue.katering.azhurkateringbackendjava.menu.exception.dish.DishAlreadyExistsException;
import azhue.katering.azhurkateringbackendjava.menu.exception.dish.DishNotFoundException;
import azhue.katering.azhurkateringbackendjava.menu.exception.image.ImageEmptyException;
import azhue.katering.azhurkateringbackendjava.menu.exception.image.ImageExtensionException;
import azhue.katering.azhurkateringbackendjava.menu.exception.image.ImageSizeException;
import azhue.katering.azhurkateringbackendjava.menu.exception.image.ImageTypeException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;


@RestControllerAdvice
public class MenuExceptionHandler {

    /**
     * Обрабатывает ошибки когда блюдо не найдено
     */
    @ExceptionHandler(DishNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleDishNotFoundException(
            DishNotFoundException ex) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.<Void>builder()
                        .success(false)
                        .message(ex.getMessage())
                        .errorCode("DISH_NOT_FOUND")
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    /**
     * Обрабатывает ошибки когда блюдо уже существует
     */
    @ExceptionHandler(DishAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Void>> handleDishAlreadyExistsException(
            DishAlreadyExistsException ex) {

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.<Void>builder()
                        .success(false)
                        .message(ex.getMessage())
                        .errorCode("DISH_ALREADY_EXISTS")
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    /**
     * Обрабатывает ошибки когда категория уже существует
     */
    @ExceptionHandler(CategoryAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Void>> handleCategoryAlreadyExistsException(
            CategoryAlreadyExistsException ex) {

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.<Void>builder()
                        .success(false)
                        .message(ex.getMessage())
                        .errorCode("CATEGORY_ALREADY_EXISTS")
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    /**
     * Обрабатывает ошибки когда категория содержит блюдо
     */
    @ExceptionHandler(CategoryHasDishesException.class)
    public ResponseEntity<ApiResponse<Void>> handleCategoryHasDishesException(
            CategoryHasDishesException ex) {

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.<Void>builder()
                        .success(false)
                        .message(ex.getMessage())
                        .errorCode("DISH_EXISTS_IN_CATEGORY")
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    /**
     * Обрабатывает ошибки когда категория не найдена
     */
    @ExceptionHandler(CategoryNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleCategoryNotFoundException(
            CategoryNotFoundException ex) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.<Void>builder()
                        .success(false)
                        .message(ex.getMessage())
                        .errorCode("CATEGORY_NOT_FOUND")
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    /**
     * Обрабатывает ошибки когда изображение пустое
     */
    @ExceptionHandler(ImageEmptyException.class)
    public ResponseEntity<ApiResponse<Void>> handleImageEmptyException(
            ImageEmptyException ex) {

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.<Void>builder()
                        .success(false)
                        .message(ex.getMessage())
                        .errorCode("IMAGE_EMPTY")
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    /**
     * Обрабатывает ошибки когда неправильное расширение файла
     */
    @ExceptionHandler(ImageExtensionException.class)
    public ResponseEntity<ApiResponse<Void>> handleImageExtensionException(
            ImageExtensionException ex) {

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.<Void>builder()
                        .success(false)
                        .message(ex.getMessage())
                        .errorCode("EXTENSION_INVALID")
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    /**
     * Обрабатывает ошибки когда размер файла больше
     */
    @ExceptionHandler(ImageSizeException.class)
    public ResponseEntity<ApiResponse<Void>> handleImageSizeException (
            ImageSizeException ex) {

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.<Void>builder()
                        .success(false)
                        .message(ex.getMessage())
                        .errorCode("IMAGE_SIZE_INVALID")
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    /**
     * Обрабатывает ошибки когда тип файла неверный
     */
    @ExceptionHandler(ImageTypeException.class)
    public ResponseEntity<ApiResponse<Void>> handleImageTypeException (
            ImageTypeException ex) {

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.<Void>builder()
                        .success(false)
                        .message(ex.getMessage())
                        .errorCode("IMAGE_TYPE_INVALID")
                        .timestamp(LocalDateTime.now())
                        .build());
    }

}
