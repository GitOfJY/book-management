package com.ex.bookmanagement.dto;

import com.ex.bookmanagement.domain.Category;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CategoryResponse {
    private Long id;
    private String name;
    private long bookCount; // 해당 카테고리에 속한 책 개수

    public static CategoryResponse fromEntity(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .bookCount(category.getBookCategories() == null ? 0 : category.getBookCategories().size())
                .build();
    }
}
