package com.ex.bookmanagement.service;

import com.ex.bookmanagement.domain.Book;
import com.ex.bookmanagement.domain.BookStatus;
import com.ex.bookmanagement.domain.Category;
import com.ex.bookmanagement.dto.BookResponse;
import com.ex.bookmanagement.dto.ChangeBookStatusRequest;
import com.ex.bookmanagement.dto.CreateBookRequest;
import com.ex.bookmanagement.exception.BusinessException;
import com.ex.bookmanagement.exception.ErrorCode;
import com.ex.bookmanagement.repository.BookRepository;
import com.ex.bookmanagement.repository.CategoryRepository;
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
 * BookService 테스트 클래스
 * 테스트 범위:
 * - 도서 전체 목록 조회
 * - 신규 도서 등록
 * - 카테고리 변경
 * - 도서 상태 변경
 * - 지은이/제목으로 검색
 * - 카테고리별 검색
 * - 도서 단권 삭제
 */
@SpringBootTest
@DisplayName("BookService 테스트")
@ActiveProfiles("test")
@Transactional
class BookServiceTest {
    @Autowired private BookService bookService;
    @Autowired private BookRepository bookRepository;
    @Autowired private CategoryRepository categoryRepository;

    // 테스트 데이터
    private Category 철학;
    private Category 예술;
    private Category 여행;
    private Category 과학;

    @BeforeEach
    void init() {
        철학 = categoryRepository.save(new Category("철학"));
        예술   = categoryRepository.save(new Category("예술"));
        여행 = categoryRepository.save(new Category("여행"));
        과학 = categoryRepository.save(new Category("과학"));
    }

    @Test
    @DisplayName("전체 도서 조회")
    void findAllBooks() {
        // given
        Long id1 = bookService.create(new CreateBookRequest("A", "작가A", BookStatus.AVAILABLE, 1, List.of(철학.getId())));
        Long id2 = bookService.create(new CreateBookRequest("B", "작가B", BookStatus.AVAILABLE, 1, List.of(예술.getId())));

        // when
        List<BookResponse> list = bookService.findAllBooks();

        // then
        assertThat(list).extracting("title").containsExactlyInAnyOrder("A", "B");
        assertThat(list).hasSize(2);
    }

    @Test
    @DisplayName("신규 도서 등록 성공 - 카테고리 필수")
    void create_success() {
        // given
        CreateBookRequest dto = new CreateBookRequest(
                "게으른 사랑", "권태영", BookStatus.AVAILABLE, 1, List.of(철학.getId(), 예술.getId())
        );

        // when
        Long id = bookService.create(dto);

        // then
        assertThat(id).isNotNull();
        Book saved = bookRepository.findById(id).orElseThrow();
        assertThat(saved.getTitle()).isEqualTo("게으른 사랑");
        assertThat(saved.getBookCategories()).hasSize(2);
    }

    @Test
    @DisplayName("신규 도서 등록 성공 - 재고 포함")
    void create_success_withStock() {
        // given
        CreateBookRequest req = new CreateBookRequest(
                "철학책 입니다",
                "아리스토텔레스",
                BookStatus.AVAILABLE,
                5,
                List.of(철학.getId())
        );

        // when
        Long bookId = bookService.create(req);

        // then
        Book saved = bookRepository.findById(bookId).orElseThrow();
        assertThat(saved.getStock()).isEqualTo(5);
        assertThat(saved.getTitle()).isEqualTo("철학책 입니다");
    }

    @Test
    @DisplayName("신규 도서 등록 - 동일 제목+저자 존재 시 신규 생성하지 않고 재고만 증가")
    void create_upsert_increaseStock_whenDuplicate() {
        // given
        Long firstId = bookService.create(new CreateBookRequest(
                "코스모스", "칼 세이건", BookStatus.AVAILABLE, 1, List.of(과학.getId())
        ));
        Book first = bookRepository.findById(firstId).orElseThrow();
        assertThat(first.getStock()).isEqualTo(1);

        // when
        Long secondId = bookService.create(new CreateBookRequest(
                "코스모스", "칼 세이건", BookStatus.AVAILABLE, 3, List.of(과학.getId())
        ));

        // then
        assertThat(secondId).isEqualTo(firstId);
        Book updated = bookRepository.findById(firstId).orElseThrow();
        assertThat(updated.getStock()).isEqualTo(4);
        assertThat(bookRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("신규 도서 등록 - 중복 등록인데 전달 재고가 0 이하이면 재고 +1 증가")
    void create_upsert_increaseByOne_whenDuplicateAndNonPositiveStock() {
        // given
        Long id = bookService.create(new CreateBookRequest(
                "여행의 기술", "알랭 드 보통", BookStatus.AVAILABLE, 2, List.of(여행.getId())
        ));
        Book before = bookRepository.findById(id).orElseThrow();
        assertThat(before.getStock()).isEqualTo(2);

        // when
        Long id2 = bookService.create(new CreateBookRequest(
                "여행의 기술", "알랭 드 보통", BookStatus.AVAILABLE, 0, List.of(여행.getId())
        ));

        // then
        assertThat(id2).isEqualTo(id);
        Book after = bookRepository.findById(id).orElseThrow();
        assertThat(after.getStock()).isEqualTo(3);
    }

    @Test
    @DisplayName("신규 도서 등록 실패 - 재고 음수")
    void create_fail_negativeStock() {
        // given
        CreateBookRequest req = new CreateBookRequest(
                "과학책입니다",
                "아인슈타인",
                BookStatus.AVAILABLE,
                -1,
                List.of(과학.getId())
        );

        // when
        BusinessException ex = assertThrows(BusinessException.class, () -> bookService.create(req));

        // then
        assertThat(ex.getCode()).isEqualTo(ErrorCode.INVALID_STOCK_QUANTITY);
    }

    @Test
    @DisplayName("신규 도서 등록 실패 - 존재하지 않는 카테고리")
    void create_fail_categoryNotFound() {
        // given
        Long invalidId = 9999L;
        CreateBookRequest dto = new CreateBookRequest(
                "단순하게 배부르게", "현영서", BookStatus.AVAILABLE, 1, List.of(철학.getId(), invalidId)
        );

        // when
        BusinessException ex = assertThrows(BusinessException.class, () -> bookService.create(dto));

        // then
        assertThat(ex.getCode()).isEqualTo(ErrorCode.CATEGORY_NOT_FOUND);
    }

    @Test
    @DisplayName("카테고리 변경 성공")
    void changeCategory_success() {
        // given
        Long bookId = bookService.create(
                new CreateBookRequest("A", "작가A", BookStatus.AVAILABLE, 1, List.of(철학.getId()))
        );

        bookService.updateCategories(bookId, List.of(여행.getId(), 과학.getId()));

        // then
        Book book = bookRepository.findById(bookId).orElseThrow();
        List<String> names = book.getBookCategories().stream()
                .map(bc -> bc.getCategory().getName())
                .toList();

        assertThat(names).containsExactly("여행", "과학");
    }

    @Test
    @DisplayName("카테고리 변경 실패 - 기존 카테고리 없음")
    void changeCategory_fail_oldCategoryNotFound() {
        // given
        Long bookId = bookService.create(
                new CreateBookRequest("A", "작가A", BookStatus.AVAILABLE, 1, List.of(철학.getId()))
        );

        // when
        BusinessException ex = assertThrows(BusinessException.class,
                () -> bookService.updateCategories(bookId, List.of(123456L))
        );

        // then
        assertThat(ex.getCode()).isEqualTo(ErrorCode.CATEGORY_NOT_FOUND);
    }

    @Test
    @DisplayName("카테고리 변경 실패 - 도서 없음")
    void changeCategory_fail_bookNotFound() {
        // given & when
        BusinessException ex = assertThrows(BusinessException.class,
                () -> bookService.updateCategories(9999L, List.of(여행.getId()))
        );

        // then
        assertThat(ex.getCode()).isEqualTo(ErrorCode.BOOK_NOT_FOUND);
        assertThat(ex).isNotNull();
    }

    @Test
    @DisplayName("도서 상태 변경 - 다른 상태로 변경")
    void changeStatus_success() {
        // given
        Long id = bookService.create(new CreateBookRequest("책", "저자", BookStatus.AVAILABLE, 1, List.of(여행.getId())));
        ChangeBookStatusRequest req = ChangeBookStatusRequest.builder()
                .status(BookStatus.SUSPENDED_DAMAGED)
                .build();

        // when
        bookService.changeStatus(id, req);

        // then
        Book book = bookRepository.findById(id).orElseThrow();
        assertThat(book.getBookStatus()).isEqualTo(BookStatus.SUSPENDED_DAMAGED);
    }

    @Test
    @DisplayName("도서 상태 변경 - 동일 상태")
    void changeStatus_noop_whenSame() {
        // given
        Long id = bookService.create(new CreateBookRequest("책", "저자", BookStatus.AVAILABLE, 1, List.of(여행.getId())));
        ChangeBookStatusRequest req = ChangeBookStatusRequest.builder()
                .status(BookStatus.AVAILABLE)
                .build();

        // when
        bookService.changeStatus(id, req);

        // then
        Book book = bookRepository.findById(id).orElseThrow();
        assertThat(book.getBookStatus()).isEqualTo(BookStatus.AVAILABLE);
    }

    @Test
    @DisplayName("도서 상태 변경 실패 - 도서 없음")
    void changeStatus_fail_bookNotFound() {
        // given
        ChangeBookStatusRequest req = ChangeBookStatusRequest.builder()
                .status(BookStatus.SUSPENDED_LOST)
                .build();

        // when
        BusinessException ex = assertThrows(BusinessException.class,
                () -> bookService.changeStatus(404L, req)
        );

        // then
        assertThat(ex).isNotNull();
        assertThat(ex.getCode()).isEqualTo(ErrorCode.BOOK_NOT_FOUND);
    }

    @Test
    @DisplayName("저자/제목 검색")
    void searchByAuthorAndTitle() {
        // given
        bookService.create(new CreateBookRequest("게으른 사랑", "권태영", BookStatus.AVAILABLE, 1, List.of(예술.getId())));
        bookService.create(new CreateBookRequest("단순하게 배부르게", "현영서", BookStatus.AVAILABLE, 1, List.of(여행.getId())));

        // when
        List<BookResponse> byAuthor = bookService.searchByAuthorAndTitle("권태영", null, 0, 10);
        List<BookResponse> byTitle  = bookService.searchByAuthorAndTitle(null, "사랑", 0, 10);

        // then
        assertThat(byAuthor).extracting(BookResponse::getAuthor).containsOnly("권태영");
        assertThat(byTitle).anyMatch(r -> r.getTitle().contains("사랑"));
    }

    @Test
    @DisplayName("카테고리별 검색 - ID/이름")
    void searchByCategory() {
        // given
        Long id1 = bookService.create(new CreateBookRequest("코스모스", "저자1", BookStatus.AVAILABLE, 1, List.of(예술.getId())));
        Long id2 = bookService.create(new CreateBookRequest("봄에 떠나는 여행", "저자2", BookStatus.AVAILABLE, 1, List.of(여행.getId())));

        // when
        List<BookResponse> 예술도서 = bookService.searchByCategory(예술.getId(), null, 0, 10);
        List<BookResponse> 여행도서   = bookService.searchByCategory(null, "여행", 0, 10);

        // then
        assertThat(예술도서).extracting(BookResponse::getTitle).contains("코스모스");
        assertThat(여행도서).extracting(BookResponse::getTitle).contains("봄에 떠나는 여행");
    }

    @Test
    @DisplayName("도서 삭제 성공 - 존재하는 도서 삭제 시 DB에서 제거됨")
    void delete_success() {
        // given
        Long id = bookService.create(new CreateBookRequest(
                "삭제할 책", "홍길동", BookStatus.AVAILABLE, 2, List.of(철학.getId())
        ));
        assertThat(bookRepository.findById(id)).isPresent();

        // when
        bookService.delete(id);

        // then
        assertThat(bookRepository.findById(id)).isEmpty();
    }

    @Test
    @DisplayName("도서 삭제 실패 - 존재하지 않는 도서 ID")
    void delete_fail_bookNotFound() {
        // given & when
        BusinessException ex = assertThrows(BusinessException.class,
                () -> bookService.delete(9999L)
        );

        // then
        assertThat(ex.getCode()).isEqualTo(ErrorCode.BOOK_NOT_FOUND);
    }

}