package com.ex.bookmanagement.dto;

import com.ex.bookmanagement.domain.Book;
import com.ex.bookmanagement.domain.BookCategory;
import com.ex.bookmanagement.domain.BookStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class CreateBookRequest {
    @Schema(description = "도서 제목", example = "객체지향의 사실과 오해", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "제목은 필수입니다.")
    private String title;

    @Schema(description = "도서 저자", example = "저자A", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "저자는 필수입니다.")
    private String author;

    @Schema(
            description = "도서 상태 (기본값: AVAILABLE)",
            example = "AVAILABLE",
            allowableValues = {"AVAILABLE", "SUSPENDED_DAMAGED", "SUSPENDED_LOST"}
    )
    private BookStatus bookStatus;

    @Min(value = 0, message = "재고는 0 이상이어야 합니다.")
    @Schema(description = "보유 재고 수량", example = "10", minimum = "0")
    private int stock;

    @Schema(
            description = "카테고리 ID 목록 (최소 1개)",
            example = "[1, 2]",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotEmpty(message = "카테고리는 최소 1개 이상이어야 합니다.")
    private List<Long> categoryIds;
}
