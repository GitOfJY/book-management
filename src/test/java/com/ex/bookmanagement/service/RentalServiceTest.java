package com.ex.bookmanagement.service;

import com.ex.bookmanagement.domain.*;
import com.ex.bookmanagement.dto.RentResponse;
import com.ex.bookmanagement.exception.BusinessException;
import com.ex.bookmanagement.exception.ErrorCode;
import com.ex.bookmanagement.repository.BookRepository;
import com.ex.bookmanagement.repository.CategoryRepository;
import com.ex.bookmanagement.repository.RentalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
/**
 * RentalService 테스트 클래스
 * 테스트 범위:
 * - 도서 대여 생성 (성공)
 * - 도서 반납 (성공)
 * - 대여 중단 (훼손/분실) (성공)
 * - 예외: 존재하지 않는 도서/대여
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class RentalServiceTest {
    @Autowired private RentalService rentalService;
    @Autowired private BookRepository bookRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private RentalRepository rentalRepository;

    private Category 철학;
    private Book book;

    @BeforeEach
    void init() {
        철학 = categoryRepository.save(new Category("철학"));
        book = Book.createBook("윤리학", "철학자A", List.of(철학), BookStatus.AVAILABLE, 1);
        bookRepository.save(book);
    }

    @Test
    @DisplayName("도서 대여 생성 성공")
    void rent_success() {
        // when
        RentResponse res = rentalService.rentBook(book.getId(), "김민철");

        // then
        assertThat(res).isNotNull();
        assertThat(res.getBookId()).isEqualTo(book.getId());
        assertThat(res.getRenterName()).isEqualTo("김민철");
        assertThat(res.getStatus()).isEqualTo(RentalStatus.RENTED);

        assertThat(rentalRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("도서 대여 생성 실패 - 존재하지 않는 도서")
    void rent_fail_bookNotFound() {
        // given
        Long invalidBookId = 9999L;

        // when
        BusinessException ex = assertThrows(BusinessException.class,
                () -> rentalService.rentBook(invalidBookId, "김민철"));

        // then
        assertThat(ex.getCode()).isEqualTo(ErrorCode.BOOK_NOT_FOUND);
    }

    @Test
    @DisplayName("도서 반납 성공")
    void return_success() {
        // given
        Rental saved = rentalRepository.save(Rental.create(book, "김민철"));

        // when
        RentResponse res = rentalService.returnBook(saved.getId());

        // then
        assertThat(res.getStatus()).isEqualTo(RentalStatus.RETURNED);
        Rental updated = rentalRepository.findById(saved.getId()).orElseThrow();
        assertThat(updated.getRentalStatus()).isEqualTo(RentalStatus.RETURNED);
        assertThat(updated.getReturnedDate()).isNotNull();
    }

    @Test
    @DisplayName("도서 반납 실패 - 존재하지 않는 대여")
    void return_fail_rentalNotFound() {
        // given
        Long invalidRentalId = 9999L;

        // when
        BusinessException ex = assertThrows(BusinessException.class,
                () -> rentalService.returnBook(invalidRentalId));

        // then
        assertThat(ex.getCode()).isEqualTo(ErrorCode.RENTAL_NOT_FOUND);
    }

    @Test
    @DisplayName("대여 중단 성공 - UNAVAILABLE (DAMAGED/LOST)")
    void suspend_success_unavailable() {
        // given
        Rental saved = rentalRepository.save(Rental.create(book, "이영희"));

        // when
        RentResponse res = rentalService.suspendRental(saved.getId());

        // then
        assertThat(res.getStatus()).isEqualTo(RentalStatus.UNAVAILABLE);

        Rental updated = rentalRepository.findById(saved.getId()).orElseThrow();
        assertThat(updated.getRentalStatus()).isEqualTo(RentalStatus.UNAVAILABLE);
    }

    @Test
    @DisplayName("대여 중단 실패 - 존재하지 않는 대여")
    void suspend_fail_rentalNotFound() {
        // given
        Long invalidRentalId = 9999L;

        // when
        BusinessException ex = assertThrows(BusinessException.class,
                () -> rentalService.suspendRental(invalidRentalId));

        // then
        assertThat(ex.getCode()).isEqualTo(ErrorCode.RENTAL_NOT_FOUND);
    }

}