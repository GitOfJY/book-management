package com.ex.bookmanagement.controller;

import com.ex.bookmanagement.dto.*;
import com.ex.bookmanagement.exception.ErrorCode;
import com.ex.bookmanagement.exception.ErrorExamples;
import com.ex.bookmanagement.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
@RequestMapping("/api/books")
public class BookController {
    private final BookService bookService;

    @GetMapping
    @Operation(summary = "전체 도서 조회 API", description = "전체 도서 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = BookResponse.class)))
    public ResponseEntity<List<BookResponse>> findBooks() {
        return ResponseEntity.ok(bookService.findAllBooks());
    }

    @PostMapping
    @Operation(summary = "신규 도서 등록 API", description = "신규 도서를 등록합니다. (카테고리 필수)")
    @ApiResponse(responseCode = "200", description = "등록 성공",
            content = @Content(schema = @Schema(implementation = Long.class)))
    @ErrorExamples({
            ErrorCode.REQUIRED_FIELD,        // 제목/저자 누락
            ErrorCode.CATEGORY_REQUIRED,     // 카테고리 최소 1개
            ErrorCode.CATEGORY_NOT_FOUND,    // 전달된 ID 중 존재하지 않음
            ErrorCode.INVALID_STOCK_QUANTITY // 음수 재고
    })
    public ResponseEntity<Long> create(@Valid @RequestBody CreateBookRequest dto) {
        return ResponseEntity.ok(bookService.create(dto));
    }

    @PutMapping("/{id}/categories")
    @Operation(summary = "도서 카테고리 변경 API", description = "기존 도서의 카테고리를 변경합니다.")
    @ApiResponse(responseCode = "204", description = "변경 성공")
    @ErrorExamples({
            ErrorCode.BOOK_NOT_FOUND,       // 도서 없음
            ErrorCode.CATEGORY_NOT_FOUND    // 요청한 카테고리 중 존재하지 않음
    })
    public ResponseEntity<Void> changeCategories(@Parameter @PathVariable("id") Long bookId,
                                                 @RequestBody UpdateCategoriesRequest req) {
        bookService.updateCategories(bookId, req.getCategoryIds());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{bookId}/status")
    @Operation(summary = "도서 상태 변경 API", description = "기존 도서의 상태를 변경합니다.")
    @ApiResponse(responseCode = "204", description = "변경 성공")
    @ErrorExamples({
            ErrorCode.BOOK_NOT_FOUND,  // 도서 없음
            ErrorCode.BOOK_STATUS_NULL // 상태 null 금지
    })
    public ResponseEntity<Void> changeStatus(@PathVariable Long bookId,
                                             @Valid @RequestBody ChangeBookStatusRequest req) {
        bookService.changeStatus(bookId, req);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    @Operation(summary = "저자 또는 제목별 도서 검색 API", description = "저자 또는 제목으로 도서를 검색합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = BookResponse.class)))
    public ResponseEntity<List<BookResponse>> searchByAuthorAndTitle(
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String title,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(bookService.searchByAuthorAndTitle(author, title, page, size));
    }

    @GetMapping("/search-by-category")
    @Operation(summary = "카테고리별 도서 검색 API", description = "카테고리ID 또는 카테고리명으로 도서를 검색합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = BookResponse.class)))
    public ResponseEntity<List<BookResponse>> searchByCategory(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String categoryName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(bookService.searchByCategory(categoryId, categoryName, page, size));
    }

    @DeleteMapping("/{bookId}")
    @Operation(summary = "도서 삭제 API", description = "도서를 삭제합니다.")
    @ApiResponse(responseCode = "204", description = "삭제 성공")
    @ErrorExamples({ErrorCode.BOOK_NOT_FOUND})
    public ResponseEntity<Void> delete(@PathVariable Long bookId) {
        bookService.delete(bookId);
        return ResponseEntity.noContent().build();
    }
}


