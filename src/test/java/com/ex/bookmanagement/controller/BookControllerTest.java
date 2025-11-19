package com.ex.bookmanagement.controller;

import com.ex.bookmanagement.domain.BookStatus;
import com.ex.bookmanagement.dto.CreateBookRequest;
import com.ex.bookmanagement.repository.BookRepository;
import com.ex.bookmanagement.repository.CategoryRepository;
import com.ex.bookmanagement.service.BookService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("BookController 통합 테스트")
class BookControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @Autowired BookService bookService;
    @Autowired CategoryRepository categoryRepository;
    @Autowired BookRepository bookRepository;

    private Long catA; // 카테고리 ID 저장용
    private Long catB;
    private Long catC;

    @BeforeEach
    void init() {
        catA = categoryRepository.save(new com.ex.bookmanagement.domain.Category("철학")).getId();
        catB = categoryRepository.save(new com.ex.bookmanagement.domain.Category("예술")).getId();
        catC = categoryRepository.save(new com.ex.bookmanagement.domain.Category("여행")).getId();
    }

    private String json(Object o) throws Exception {
        return objectMapper.writeValueAsString(o);
    }

    private Long createBook(String title, String author, int stock, BookStatus status, List<Long> categoryIds) {
        return bookService.create(new CreateBookRequest(title, author, status, stock, categoryIds));
    }

    @Test
    @DisplayName("도서 전체 조회 - 200 OK & 배열")
    void findBooks_success() throws Exception {
        createBook("A", "작가A", 1, BookStatus.AVAILABLE, List.of(catA));
        createBook("B", "작가B", 2, BookStatus.AVAILABLE, List.of(catB));

        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", isA(List.class)))
                .andExpect(jsonPath("$.length()", greaterThanOrEqualTo(2)));
    }

    @Test
    @DisplayName("신규 도서 등록 성공 - 200 OK & ID 반환")
    void create_success() throws Exception {
        var payload = Map.of(
                "title", "게으른 사랑",
                "author", "권태영",
                "bookStatus", "AVAILABLE",
                "stock", 3,
                "categoryIds", List.of(catA, catB)
        );

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(payload)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().string(not(blankOrNullString())));
    }

    @Test
    @DisplayName("신규 도서 등록 실패 - 존재하지 않는 카테고리(404)")
    void create_fail_categoryNotFound() throws Exception {
        var payload = Map.of(
                "title", "단순하게 배부르게",
                "author", "현영서",
                "bookStatus", "AVAILABLE",
                "stock", 1,
                "categoryIds", List.of(catA, 999999L)
        );

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(payload)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("CATEGORY_NOT_FOUND"));
    }

    @Test
    @DisplayName("신규 도서 등록 실패 - 음수 재고(400)")
    void create_fail_negativeStock() throws Exception {
        var payload = Map.of(
                "title", "과학책",
                "author", "아인슈타인",
                "bookStatus", "AVAILABLE",
                "stock", -1,
                "categoryIds", List.of(catC)
        );

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(payload)))
                .andExpect(status().isBadRequest())
                // 프로젝트의 예외 바인딩 정책에 맞춰 아래 중 택1
                .andExpect(jsonPath("$.code").value("INVALID_ARGUMENT"));
    }

    @Test
    @DisplayName("카테고리 변경 성공 - 204 No Content")
    void updateCategories_success() throws Exception {
        Long bookId = createBook("A", "작가A", 1, BookStatus.AVAILABLE, List.of(catA));

        var payload = Map.of("categoryIds", List.of(catB, catC));

        mockMvc.perform(put("/api/books/{id}/categories", bookId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(payload)))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("카테고리 변경 실패 - 도서 없음(404)")
    void updateCategories_fail_bookNotFound() throws Exception {
        var payload = Map.of("categoryIds", List.of(catA));

        mockMvc.perform(put("/api/books/{id}/categories", 999999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(payload)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("BOOK_NOT_FOUND"));
    }

    @Test
    @DisplayName("카테고리 변경 실패 - 요청 리스트 중 일부 존재하지 않음(404)")
    void updateCategories_fail_categoryNotFound() throws Exception {
        Long bookId = createBook("B", "작가B", 1, BookStatus.AVAILABLE, List.of(catA));

        var payload = Map.of("categoryIds", List.of(catA, 123456789L));

        mockMvc.perform(put("/api/books/{id}/categories", bookId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(payload)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("CATEGORY_NOT_FOUND"));
    }

    @Test
    @DisplayName("도서 상태 변경 성공 - 204 No Content")
    void changeStatus_success() throws Exception {
        Long bookId = createBook("C", "저자", 1, BookStatus.AVAILABLE, List.of(catA));

        var payload = Map.of("status", "SUSPENDED_DAMAGED");

        mockMvc.perform(put("/api/books/{bookId}/status", bookId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(payload)))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("도서 상태 변경 실패 - 도서 없음(404)")
    void changeStatus_fail_bookNotFound() throws Exception {
        var payload = Map.of("status", "SUSPENDED_LOST");

        mockMvc.perform(put("/api/books/{bookId}/status", 999999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(payload)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("BOOK_NOT_FOUND"));
    }

    @Test
    @DisplayName("저자/제목 검색 - 200 OK")
    void searchByAuthorAndTitle_success() throws Exception {
        createBook("게으른 사랑", "권태영", 1, BookStatus.AVAILABLE, List.of(catA));
        createBook("단순하게 배부르게", "현영서", 1, BookStatus.AVAILABLE, List.of(catB));

        mockMvc.perform(get("/api/books/search")
                        .param("author", "권태영")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].author", everyItem(equalTo("권태영"))));
    }

    @Test
    @DisplayName("카테고리 검색 - ID로 검색 200 OK")
    void searchByCategory_byId_success() throws Exception {
        createBook("코스모스", "저자1", 1, BookStatus.AVAILABLE, List.of(catA));
        createBook("여행의 기술", "저자2", 1, BookStatus.AVAILABLE, List.of(catC));

        mockMvc.perform(get("/api/books/search-by-category")
                        .param("categoryId", String.valueOf(catC))
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].categories", notNullValue()));
    }

    @Test
    @DisplayName("도서 삭제 성공 - 204 No Content")
    void delete_success() throws Exception {
        Long id = createBook("지우개책", "삭제저자", 1, BookStatus.AVAILABLE, List.of(catB));
        Assertions.assertTrue(bookRepository.findById(id).isPresent());

        mockMvc.perform(delete("/api/books/{bookId}", id))
                .andExpect(status().isNoContent());

        Assertions.assertTrue(bookRepository.findById(id).isEmpty());
    }

    @Test
    @DisplayName("도서 삭제 실패 - 도서 없음(404)")
    void delete_fail_notFound() throws Exception {
        mockMvc.perform(delete("/api/books/{bookId}", 999999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("BOOK_NOT_FOUND"));
    }
}
