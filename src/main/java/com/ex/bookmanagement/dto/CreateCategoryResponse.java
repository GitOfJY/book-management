package com.ex.bookmanagement.dto;

import com.ex.bookmanagement.domain.Category;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CreateCategoryResponse {
    private Long id;
    private String name;
    private long bookCount;

    public static CategoryResponse fromEntity(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .bookCount(category.getBookCategories() == null ? 0 : category.getBookCategories().size())
                .build();
    }
}
