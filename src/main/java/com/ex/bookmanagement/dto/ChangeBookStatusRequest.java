package com.ex.bookmanagement.dto;

import com.ex.bookmanagement.domain.BookStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChangeBookStatusRequest {
    @NotNull
    @Schema(
            description = "변경할 도서 상태",
            example = "AVAILABLE",
            allowableValues = {"AVAILABLE", "SUSPENDED_DAMAGED", "SUSPENDED_LOST"}
    )
    private BookStatus status;
}
