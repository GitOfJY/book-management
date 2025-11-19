package com.ex.bookmanagement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCategoriesRequest {
    @NotEmpty(message = "카테고리 ID 리스트는 최소 1개 이상이어야 합니다.")
    @Valid
    @Schema(
            description = "도서에 적용할 카테고리 ID 리스트 (기존 카테고리를 새로 지정)",
            example = "[1, 2, 5]",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private List<Long> categoryIds;
}