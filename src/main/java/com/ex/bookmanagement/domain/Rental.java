package com.ex.bookmanagement.domain;

import com.ex.bookmanagement.exception.BusinessException;
import com.ex.bookmanagement.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Rental {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(nullable = false, length = 50)
    private String renterName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private RentalStatus rentalStatus;

    @Column(nullable = false)
    private LocalDateTime rentedDate;

    @Column(nullable = false)
    private LocalDateTime dueDate;

    private LocalDateTime returnedDate;

    public static Rental create(Book book, String renterName) {
        return Rental.builder()
                .book(book)
                .renterName(renterName)
                .rentalStatus(RentalStatus.RENTED)
                .rentedDate(LocalDateTime.now())
                .dueDate(LocalDateTime.now().plusWeeks(2))
                .build();
    }

    /** 반납 처리 */
    public void returnBook() {
        if (!this.rentalStatus.isActive()) {
            throw new BusinessException(ErrorCode.ALREADY_RETURNED_OR_UNAVAILABLE);
        }
        this.rentalStatus = RentalStatus.RETURNED;
        this.returnedDate = LocalDateTime.now();
    }

    /** 대여 불가능 상태로 표시 (도서 훼손/분실 등) */
    public void markUnavailable() {
        if (!this.rentalStatus.isActive()) {
            throw new BusinessException(ErrorCode.ALREADY_RETURNED_OR_UNAVAILABLE);
        }
        this.rentalStatus = RentalStatus.UNAVAILABLE;
    }
}
