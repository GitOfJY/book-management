package com.ex.bookmanagement.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.Instant;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "ErrorResponse", description = "에러 응답")
public class ErrorResponse {
    @Schema(description = "에러 코드", example = "BOOK_NOT_FOUND")
    private String code;

    @Schema(description = "에러 메시지", example = "존재하지 않는 도서입니다.")
    private String message;

    @Schema(description = "HTTP 상태 코드", example = "404")
    private int status;

    @Schema(description = "요청 경로", example = "/api/books/9999")
    private String path;

    @Schema(description = "에러 발생 시각(UTC)", example = "2025-11-11T10:00:00Z")
    private Instant timestamp;

    @Schema(description = "추가 정보", example = "{\"bookId\": 9999, \"field\": \"id\"}")
    private Map<String, Object> args;

    public static ErrorResponse of(String code, String message, int status, String path, Map<String, Object> args) {
        ErrorResponse e = new ErrorResponse();
        e.code = code;
        e.message = message;
        e.status = status;
        e.path = path;
        e.args = args;
        e.timestamp = Instant.now();
        return e;
    }
}
