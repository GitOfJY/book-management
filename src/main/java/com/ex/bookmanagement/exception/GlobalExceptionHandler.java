package com.ex.bookmanagement.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException e, HttpServletRequest req) {
        ErrorCode code = e.getCode();
        ErrorResponse body = ErrorResponse.builder()
                .code(code.name())
                .message(e.getMessage())
                .status(code.status().value())
                .path(req.getRequestURI())
                .timestamp(Instant.now())
                .args(e.getArgs().isEmpty() ? null : e.getArgs())
                .build();
        log.debug("[{}] {}", code.name(), e.getMessage());
        return ResponseEntity.status(code.status()).body(body);
    }

    // 예상 못한 예외는 500으로
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleEtc(Exception e, HttpServletRequest req) {
        log.error("Unhandled exception", e);
        ErrorResponse body = ErrorResponse.builder()
                .code("INTERNAL_SERVER_ERROR")
                .message("서버 오류가 발생했습니다.")
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .path(req.getRequestURI())
                .timestamp(Instant.now())
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                      HttpServletRequest req) {

        List<Map<String, Object>> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("field", fe.getField());
                    m.put("code", fe.getCode());
                    m.put("msg", fe.getDefaultMessage());
                    return m;
                })
                .collect(Collectors.toList());

        Map<String, Object> args = new LinkedHashMap<>();
        args.put("errors", errors);

        ErrorCode ec = ErrorCode.INVALID_ARGUMENT;
        return ErrorResponse.of(
                ec.name(),
                ec.defaultMessage(),
                ec.status().value(),
                req.getRequestURI(),
                args
        );
    }
}
