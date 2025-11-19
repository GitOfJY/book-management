package com.ex.bookmanagement.controller;

import com.ex.bookmanagement.dto.CategoryResponse;
import com.ex.bookmanagement.dto.CreateCategoryRequest;
import com.ex.bookmanagement.exception.ErrorCode;
import com.ex.bookmanagement.exception.ErrorExamples;
import com.ex.bookmanagement.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/categories")
public class CategoryController {
    private final CategoryService categoryService;

    @GetMapping
    @Operation(summary = "전체 카테고리 조회 API", description = "전체 카테고리를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = CategoryResponse.class)))
    public ResponseEntity<List<CategoryResponse>> findCategories() {
        return ResponseEntity.ok(categoryService.findAllCategories());
    }

    @PostMapping
    @Operation(summary = "신규 카테고리 추가", description = "새 카테고리를 생성합니다.")
    @ApiResponse(responseCode = "200", description = "생성 성공",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = CategoryResponse.class)))
    @ErrorExamples({
            ErrorCode.REQUIRED_FIELD,
            ErrorCode.CATEGORY_ALREADY_EXISTS,
            ErrorCode.INVALID_ARGUMENT
    })
    public ResponseEntity<CategoryResponse> createCategory(@Valid @RequestBody CreateCategoryRequest req) {
        return ResponseEntity.ok(categoryService.create(req));
    }

    @DeleteMapping("/{categoryId}")
    @Operation(summary = "카테고리 삭제 API", description = "카테고리를 삭제합니다.")
    @ApiResponse(responseCode = "204", description = "삭제 성공")
    @ErrorExamples({ErrorCode.CATEGORY_NOT_FOUND})
    public ResponseEntity<Void> delete(@PathVariable Long categoryId) {
        categoryService.delete(categoryId);
        return ResponseEntity.noContent().build();
    }
}
