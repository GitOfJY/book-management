package com.ex.bookmanagement.controller;

import com.ex.bookmanagement.dto.RentRequest;
import com.ex.bookmanagement.dto.RentResponse;
import com.ex.bookmanagement.exception.ErrorCode;
import com.ex.bookmanagement.exception.ErrorExamples;
import com.ex.bookmanagement.service.RentalService;
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
@RequestMapping("/api/rentals")
public class RentalController {
    private final RentalService rentalService;

    @PostMapping
    @Operation(summary = "도서 대여 API", description = "bookId와 이름으로 대여를 생성합니다.")
    @ApiResponse(
            responseCode = "200",
            description = "대여 성공",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = RentResponse.class))
    )
    @ErrorExamples({
            ErrorCode.BOOK_NOT_FOUND,
            ErrorCode.BOOK_NOT_AVAILABLE,
            ErrorCode.OUT_OF_STOCK
    })
    public ResponseEntity<RentResponse> rent(@Valid @RequestBody RentRequest req) {
        RentResponse response = rentalService.rentBook(req.getBookId(), req.getRenterName());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{rentalId}/return")
    @Operation(summary = "도서 반납 API", description = "대여건을 반납 처리합니다.")
    @ApiResponse(responseCode = "204", description = "반납 완료")
    @ErrorExamples({
            ErrorCode.RENTAL_NOT_FOUND,
            ErrorCode.ALREADY_RETURNED_OR_UNAVAILABLE
    })
    public ResponseEntity<Void> returnBook(@Parameter(description = "대여 ID") @PathVariable Long rentalId) {
        rentalService.returnBook(rentalId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{rentalId}/suspend")
    @Operation(summary = "대여 중단 API", description = "훼손/분실 등의 사유로 대여를 중단 처리합니다.")
    @ApiResponse(responseCode = "204", description = "대여 중단 완료")
    @ErrorExamples({
            ErrorCode.RENTAL_NOT_FOUND,
            ErrorCode.ALREADY_RETURNED_OR_UNAVAILABLE
    })
    public ResponseEntity<Void> suspend(@PathVariable Long rentalId) {
        rentalService.suspendRental(rentalId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(summary = "전체 대여 내역 조회", description = "전체 대여 내역 목록을 조회합니다.")
    @ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = RentResponse.class))
    )
    public ResponseEntity<List<RentResponse>> findAll() {
        return ResponseEntity.ok(rentalService.findAll());
    }
}
