package com.ex.bookmanagement.controller;

import com.ex.bookmanagement.domain.BookStatus;
import com.ex.bookmanagement.dto.CreateBookRequest;
import com.ex.bookmanagement.repository.BookRepository;
import com.ex.bookmanagement.repository.CategoryRepository;
import com.ex.bookmanagement.service.BookService;
import com.ex.bookmanagement.service.RentalService;
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
@DisplayName("RentalController 통합 테스트")
class RentalControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @Autowired BookService bookService;
    @Autowired RentalService rentalService;

    @Autowired CategoryRepository categoryRepository;
    @Autowired BookRepository bookRepository;

    private Long catEtcId;

    @BeforeEach
    void init() {
        catEtcId = categoryRepository.save(new com.ex.bookmanagement.domain.Category("기타")).getId();
    }

    private Long createBook(String title, String author, int stock, BookStatus status) {
        return bookService.create(new CreateBookRequest(
                title, author, status, stock, List.of(catEtcId)
        ));
    }

    private String json(Object o) throws Exception {
        return objectMapper.writeValueAsString(o);
    }

    @Test
    @DisplayName("대여 성공")
    void rent_success() throws Exception {
        Long bookId = createBook("Clean Code", "Robert", 3, BookStatus.AVAILABLE);

        mockMvc.perform(post("/api/rentals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "bookId", bookId,
                                "renterName", "김민철"
                        ))))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.bookId").value(bookId))
                .andExpect(jsonPath("$.renterName").value("김민철"))
                .andExpect(jsonPath("$.status").value("RENTED"))
                .andExpect(jsonPath("$.rentedAt", notNullValue()))
                .andExpect(jsonPath("$.returnedAt", notNullValue()));
    }

    @Test
    @DisplayName("대여 실패 - 존재하지 않는 도서(404)")
    void rent_fail_bookNotFound() throws Exception {
        mockMvc.perform(post("/api/rentals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "bookId", 999999L,
                                "renterName", "대여자"
                        ))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("BOOK_NOT_FOUND"));
    }

    @Test
    @DisplayName("대여 실패 - 재고 부족(400 OUT_OF_STOCK)")
    void rent_fail_outOfStock() throws Exception {
        Long bookId = createBook("테스트책", "저자", 0, BookStatus.AVAILABLE);

        mockMvc.perform(post("/api/rentals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "bookId", bookId,
                                "renterName", "대여자"
                        ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("OUT_OF_STOCK"));
    }

    @Test
    @DisplayName("대여 실패 - 도서 상태 불가(400 BOOK_NOT_AVAILABLE)")
    void rent_fail_bookNotAvailable() throws Exception {
        Long bookId = createBook("테스트책", "저자", 1, BookStatus.SUSPENDED_DAMAGED);

        mockMvc.perform(post("/api/rentals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "bookId", bookId,
                                "renterName", "대여자"
                        ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BOOK_NOT_AVAILABLE"));
    }

    @Test
    @DisplayName("반납 성공 - 204 No Content")
    void return_success() throws Exception {
        Long bookId = createBook("테스트책", "저자", 1, BookStatus.AVAILABLE);
        Long rentalId = rentalService.rentBook(bookId, "대여자").getRentalId();

        mockMvc.perform(put("/api/rentals/{rentalId}/return", rentalId))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("반납 실패 - 대여 없음(404 RENTAL_NOT_FOUND)")
    void return_fail_notFound() throws Exception {
        mockMvc.perform(put("/api/rentals/{rentalId}/return", 999999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RENTAL_NOT_FOUND"));
    }

    @Test
    @DisplayName("반납 실패 - 이미 반납/중단된 상태(400 ALREADY_RETURNED_OR_UNAVAILABLE)")
    void return_fail_alreadyReturned() throws Exception {
        Long bookId = createBook("테스트책", "저자", 1, BookStatus.AVAILABLE);
        Long rentalId = rentalService.rentBook(bookId, "대여자").getRentalId();

        // 1차 반납 성공
        mockMvc.perform(put("/api/rentals/{rentalId}/return", rentalId))
                .andExpect(status().isNoContent());

        // 2차 반납 -> 실패
        mockMvc.perform(put("/api/rentals/{rentalId}/return", rentalId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("ALREADY_RETURNED_OR_UNAVAILABLE"));
    }

    @Test
    @DisplayName("대여 중단 성공 - 204 No Content")
    void suspend_success() throws Exception {
        Long bookId = createBook("테스트책", "저자", 1, BookStatus.AVAILABLE);
        Long rentalId = rentalService.rentBook(bookId, "대여자").getRentalId();

        mockMvc.perform(put("/api/rentals/{rentalId}/suspend", rentalId))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("대여 중단 실패 - 대여 없음(404)")
    void suspend_fail_notFound() throws Exception {
        mockMvc.perform(put("/api/rentals/{rentalId}/suspend", 999999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RENTAL_NOT_FOUND"));
    }

    @Test
    @DisplayName("대여 중단 실패 - 이미 반납/중단된 상태(400)")
    void suspend_fail_alreadyClosed() throws Exception {
        Long bookId = createBook("테스트책", "저자", 1, BookStatus.AVAILABLE);
        Long rentalId = rentalService.rentBook(bookId, "대여자").getRentalId();

        // 1차 중단 성공
        mockMvc.perform(put("/api/rentals/{rentalId}/suspend", rentalId))
                .andExpect(status().isNoContent());

        // 2차 중단 -> 실패
        mockMvc.perform(put("/api/rentals/{rentalId}/suspend", rentalId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("ALREADY_RETURNED_OR_UNAVAILABLE"));
    }

    @Test
    @DisplayName("전체 대여 내역 조회 - 200 OK & 배열")
    void findAll_success() throws Exception {
        Long id1 = createBook("A", "작가A", 2, BookStatus.AVAILABLE);
        Long id2 = createBook("B", "작가B", 1, BookStatus.AVAILABLE);

        rentalService.rentBook(id1, "대여자A");
        rentalService.rentBook(id2, "대여자B");

        mockMvc.perform(get("/api/rentals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", isA(List.class)))
                .andExpect(jsonPath("$.length()", greaterThanOrEqualTo(2)));
    }
}

