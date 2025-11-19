package com.ex.bookmanagement.repository;

import com.ex.bookmanagement.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("BookRepository 테스트")
class BookRepositoryTest {

    @Autowired BookRepository bookRepository;
    @Autowired CategoryRepository categoryRepository;

    private Category 철학;
    private Category 예술;
    private Category 여행;

    private Long bookAId; // "게으른 사랑", 권태영, [예술]
    private Long bookBId; // "단순하게 배부르게", 현영서, [여행]
    private Long bookCId; // "철학책입니다", 아리스토텔레스, [철학, 예술]

    @BeforeEach
    void init() {
        철학 = categoryRepository.save(new Category("철학"));
        예술 = categoryRepository.save(new Category("예술"));
        여행 = categoryRepository.save(new Category("여행"));

        // A
        Book bookA = Book.createBook(
                "게으른 사랑", "권태영",
                List.of(예술),
                BookStatus.AVAILABLE, 3
        );
        bookAId = bookRepository.save(bookA).getId();

        // B
        Book bookB = Book.createBook(
                "단순하게 배부르게", "현영서",
                List.of(여행),
                BookStatus.AVAILABLE, 2
        );
        bookBId = bookRepository.save(bookB).getId();

        // C (다중 카테고리)
        Book bookC = Book.createBook(
                "철학책입니다", "아리스토텔레스",
                List.of(철학, 예술),
                BookStatus.AVAILABLE, 1
        );
        bookCId = bookRepository.save(bookC).getId();
    }

    @Test
    @DisplayName("저자/제목 검색 - 저자 부분일치")
    void searchByAuthorAndTitle_authorOnly() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "id"));
        Page<Book> page = bookRepository.searchByAuthorAndTitle("권태", null, pageable);

        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().get(0).getAuthor()).isEqualTo("권태영");
    }

    @Test
    @DisplayName("저자/제목 검색 - 제목 부분일치")
    void searchByAuthorAndTitle_titleOnly() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "id"));
        Page<Book> page = bookRepository.searchByAuthorAndTitle(null, "사랑", pageable);

        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().get(0).getTitle()).contains("사랑");
    }

    @Test
    @DisplayName("저자/제목 검색 - 둘 다 null이면 전체")
    void searchByAuthorAndTitle_bothNull() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "id"));
        Page<Book> page = bookRepository.searchByAuthorAndTitle(null, null, pageable);

        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getContent()).extracting(Book::getId)
                .containsExactlyInAnyOrder(bookAId, bookBId, bookCId);
    }

    @Test
    @DisplayName("카테고리 검색 - ID로 검색")
    void searchByCategory_byId() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "id"));
        Page<Book> page = bookRepository.searchByCategory(예술.getId(), null, pageable);

        // 예술 카테고리: bookA, bookC
        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent()).extracting(Book::getId)
                .containsExactlyInAnyOrder(bookAId, bookCId);
    }

    @Test
    @DisplayName("카테고리 검색 - 이름 부분일치")
    void searchByCategory_byName() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "id"));
        Page<Book> page = bookRepository.searchByCategory(null, "여행", pageable);

        // 여행 카테고리: bookB
        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().get(0).getId()).isEqualTo(bookBId);
    }

    @Test
    @DisplayName("findByIdWithCategories - 카테고리 페치조인 확인")
    void findByIdWithCategories_success() {
        Optional<Book> opt = bookRepository.findByIdWithCategories(bookCId);
        assertThat(opt).isPresent();

        Book loaded = opt.get();

        assertThat(loaded.getBookCategories()).hasSize(2);
        assertThat(loaded.getBookCategories())
                .extracting(bc -> bc.getCategory().getName())
                .containsExactlyInAnyOrder("철학", "예술");
    }

    @Test
    @DisplayName("findByTitleAndAuthor - 존재하는 도서")
    void findByTitleAndAuthor_found() {
        Optional<Book> opt = bookRepository.findByTitleAndAuthor("게으른 사랑", "권태영");
        assertThat(opt).isPresent();
        assertThat(opt.get().getId()).isEqualTo(bookAId);
    }

    @Test
    @DisplayName("findByTitleAndAuthor - 존재하지 않음")
    void findByTitleAndAuthor_notFound() {
        Optional<Book> opt = bookRepository.findByTitleAndAuthor("없는책", "없는저자");
        assertThat(opt).isEmpty();
    }
}
