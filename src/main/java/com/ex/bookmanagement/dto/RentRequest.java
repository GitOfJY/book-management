package com.ex.bookmanagement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RentRequest {
    @NotNull(message = "bookId는 필수입니다.")
    @Schema(description = "대여할 도서 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long bookId;

    @NotBlank(message = "대여자 이름은 필수입니다.")
    @Schema(description = "대여자 이름", example = "김민철", requiredMode = Schema.RequiredMode.REQUIRED)
    private String renterName;
}
