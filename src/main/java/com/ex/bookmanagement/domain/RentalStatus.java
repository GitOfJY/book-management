package com.ex.bookmanagement.domain;

public enum RentalStatus {
    RENTED,                 // 대여 중
    RETURNED,               // 정상 반납
    UNAVAILABLE,            // 대여 불가
    ;

    public boolean isActive() {
        return this == RENTED;
    }
}
