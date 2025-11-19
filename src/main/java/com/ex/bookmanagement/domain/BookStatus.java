package com.ex.bookmanagement.domain;

public enum BookStatus {
    AVAILABLE,         // 정상
    SUSPENDED_DAMAGED, // 훼손
    SUSPENDED_LOST,    // 분실
    ;

    public boolean isRentable() {
        return this == AVAILABLE;
    }
}
