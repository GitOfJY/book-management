package com.ex.bookmanagement.service;

import com.ex.bookmanagement.domain.Book;
import com.ex.bookmanagement.domain.Rental;
import com.ex.bookmanagement.domain.RentalStatus;
import com.ex.bookmanagement.dto.RentResponse;
import com.ex.bookmanagement.exception.BusinessException;
import com.ex.bookmanagement.exception.ErrorCode;
import com.ex.bookmanagement.repository.BookRepository;
import com.ex.bookmanagement.repository.RentalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
@RequiredArgsConstructor
public class RentalService {
    private final RentalRepository rentalRepository;
    private final BookRepository bookRepository;

    /** 대여 */
    public RentResponse rentBook(Long bookId, String renterName) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BOOK_NOT_FOUND, Map.of("id", bookId)));

        // BookStatus에서 직접 대여 가능 여부 판단
        if (!book.getBookStatus().isRentable()) {
            if (book.getStock() <= 0) {
                throw new BusinessException(ErrorCode.OUT_OF_STOCK, Map.of("id", bookId));
            }
            throw new BusinessException(ErrorCode.BOOK_NOT_AVAILABLE, Map.of("id", bookId));
        }

        book.decreaseStock(1);
        Rental rental = Rental.create(book, renterName);
        return RentResponse.fromEntity(rentalRepository.save(rental));
    }

    /** 반납 */
    public RentResponse returnBook(Long rentalId) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.RENTAL_NOT_FOUND, Map.of("rentalId", rentalId))
                );

        rental.returnBook();
        rental.getBook().increaseStock(1);
        return RentResponse.fromEntity(rental);
    }

    /** 대여 중단 (훼손/분실 등) */
    public RentResponse suspendRental(Long rentalId) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.RENTAL_NOT_FOUND, Map.of("rentalId", rentalId))
                );

        // 이미 반납되었거나 취소된 건은 중단 불가
        if (rental.getRentalStatus() == RentalStatus.RETURNED) {
            throw new BusinessException(ErrorCode.INVALID_RENTAL_SUSPEND_REASON, Map.of("status", rental.getRentalStatus()));
        }

        // 대여 불가 상태로 전환
        rental.markUnavailable();
        return RentResponse.fromEntity(rental);
    }

    /** 전체 대여 내역 조회 */
    public List<RentResponse> findAll() {
        // N+1 방지 위해 book까지 함께 조회 (Repository에서 @EntityGraph or fetch join 처리)
        List<Rental> rentals = rentalRepository.findAllWithBook();

        // 최근 대여순 정렬
        rentals.sort(Comparator.comparing(Rental::getRentedDate).reversed());

        return rentals.stream()
                .map(RentResponse::fromEntity)
                .toList();
    }
}
