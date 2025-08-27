package azhue.katering.azhurkateringbackendjava.menu.model.entity;

import azhue.katering.azhurkateringbackendjava.common.model.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Сущность категории блюд
 *
 * @version 1.0.0
 */
@Entity
@Table(name = "categories")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Category extends BaseEntity {
    
    @Column(name = "name", unique = true, nullable = false, length = 100)
    private String name;
    
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = false;
    
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Dish> dishes = new ArrayList<>();


    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        Category category = (Category) object;
        return Objects.equals(name, category.name) && Objects.equals(isActive, category.isActive) && Objects.equals(dishes, category.dishes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, isActive, dishes);
    }

    @Override
    public String toString() {
        return "Category{" +
                "name='" + name + '\'' +
                ", isActive=" + isActive +
                ", dishes=" + dishes +
                '}';
    }
}

