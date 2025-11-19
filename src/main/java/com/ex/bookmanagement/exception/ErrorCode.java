package com.ex.bookmanagement.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    INVALID_ARGUMENT(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    REQUIRED_FIELD(HttpStatus.BAD_REQUEST, "{field}은(는) 필수입니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "리소스를 찾을 수 없습니다."),

    BOOK_NOT_FOUND(HttpStatus.NOT_FOUND, "도서를 찾을 수 없습니다. (id={id})"),
    BOOK_STATUS_NULL(HttpStatus.BAD_REQUEST, "도서 상태는 null로 변경할 수 없습니다."),
    BOOK_NOT_AVAILABLE(HttpStatus.BAD_REQUEST, "도서가 대여 가능한 상태가 아닙니다."),

    OUT_OF_STOCK(HttpStatus.BAD_REQUEST, "해당 도서는 재고가 부족합니다. (id={id})"),
    INVALID_STOCK_QUANTITY(HttpStatus.BAD_REQUEST, "재고 수량은 음수가 될 수 없습니다."),
    STOCK_INCREASE_AMOUNT_INVALID(HttpStatus.BAD_REQUEST, "증가 수량은 1 이상이어야 합니다."),
    STOCK_DECREASE_AMOUNT_INVALID(HttpStatus.BAD_REQUEST, "감소 수량은 1 이상이어야 합니다."),

    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "카테고리를 찾을 수 없습니다. (id={id})"),
    CATEGORY_NULL(HttpStatus.BAD_REQUEST, "카테고리가 null일 수 없습니다."),
    CATEGORY_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 존재하는 카테고리입니다: {name}"),
    CATEGORY_REQUIRED(HttpStatus.BAD_REQUEST, "신규 도서는 최소 한 개 이상의 카테고리가 필요합니다."),
    BOOK_CATEGORY_NOT_LINKED(HttpStatus.BAD_REQUEST, "도서에 기존 카테고리가 연결되어 있지 않습니다."),

    RENTAL_NOT_FOUND(HttpStatus.NOT_FOUND, "대여 정보를 찾을 수 없습니다. (id={id})"),
    INVALID_RENTAL_SUSPEND_REASON(HttpStatus.BAD_REQUEST, "유효하지 않은 대여 중단 사유입니다."),
    ALREADY_RETURNED_OR_UNAVAILABLE(HttpStatus.BAD_REQUEST, "이미 반납되었거나 대여 중단된 도서입니다."),

    ;

    private final HttpStatus status;
    private final String defaultMessage;

    ErrorCode(HttpStatus status, String defaultMessage) {
        this.status = status;
        this.defaultMessage = defaultMessage;
    }

    public HttpStatus status() {
        return status;
    }

    public String defaultMessage() {
        return defaultMessage;
    }
}
