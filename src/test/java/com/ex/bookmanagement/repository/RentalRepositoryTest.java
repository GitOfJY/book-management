package com.ex.bookmanagement.repository;

import com.ex.bookmanagement.domain.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("RentalRepository 테스트")
class RentalRepositoryTest {

    @Autowired RentalRepository rentalRepository;
    @Autowired BookRepository bookRepository;
    @Autowired CategoryRepository categoryRepository;

    @Test
    @DisplayName("대여 목록과 함께 Book을 페치 조인으로 로딩")
    void findAllWithBook_fetchJoin() {
        // given
        Category travel = categoryRepository.save(new Category("여행"));
        Category art    = categoryRepository.save(new Category("예술"));

        Book book1 = Book.createBook("봄에 떠나는 여행", "저자A", List.of(travel), BookStatus.AVAILABLE, 3);
        Book book2 = Book.createBook("예술의 이해", "저자B", List.of(art), BookStatus.AVAILABLE, 2);
        book1 = bookRepository.save(book1);
        book2 = bookRepository.save(book2);

        Rental r1 = rentalRepository.save(Rental.create(book1, "대여자A"));
        Rental r2 = rentalRepository.save(Rental.create(book2, "대여자B"));

        // when
        List<Rental> rentals = rentalRepository.findAllWithBook();

        // then
        assertThat(rentals).hasSize(2);
        // 각 Rental에 Book이 함께 로드되었는지 확인 (null 아님 + 필드 접근 가능)
        assertThat(rentals).extracting(r -> r.getBook().getTitle())
                .containsExactlyInAnyOrder("봄에 떠나는 여행", "예술의 이해");

        // 생성자 값들도 정상 셋업 확인
        assertThat(rentals).extracting(Rental::getRenterName)
                .containsExactlyInAnyOrder("대여자A", "대여자B");
        assertThat(rentals).allMatch(r -> r.getRentalStatus() == RentalStatus.RENTED);
    }

    @Test
    @DisplayName("대여가 없으면 빈 리스트 반환")
    void findAllWithBook_empty() {
        List<Rental> rentals = rentalRepository.findAllWithBook();
        assertThat(rentals).isEmpty();
    }
}