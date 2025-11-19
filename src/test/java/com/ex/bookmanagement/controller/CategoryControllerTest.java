package com.ex.bookmanagement.controller;

import com.ex.bookmanagement.dto.CreateCategoryRequest;
import com.ex.bookmanagement.repository.CategoryRepository;
import com.ex.bookmanagement.service.CategoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("CategoryController 통합 테스트")
class CategoryControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @Autowired CategoryService categoryService;
    @Autowired CategoryRepository categoryRepository;

    private Long catPhilosophyId;
    private Long catArtId;

    @BeforeEach
    void init() {
        catPhilosophyId = categoryService.create(new CreateCategoryRequest("철학")).getId();
        catArtId        = categoryService.create(new CreateCategoryRequest("예술")).getId();
    }

    private String json(Object o) throws Exception {
        return objectMapper.writeValueAsString(o);
    }

    @Test
    @DisplayName("전체 카테고리 조회 - 200 OK & 배열")
    void findCategories_success() throws Exception {
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", isA(java.util.List.class)))
                .andExpect(jsonPath("$.length()", greaterThanOrEqualTo(2)))
                .andExpect(jsonPath("$[*].name", hasItems("철학", "예술")));
    }

    @Test
    @DisplayName("신규 카테고리 생성 성공 - 200 OK & CategoryResponse")
    void create_success() throws Exception {
        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("name", "여행"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name").value("여행"));
    }

    @Test
    @DisplayName("신규 카테고리 생성 실패 - 중복 이름(409 CATEGORY_ALREADY_EXISTS)")
    void create_fail_duplicate() throws Exception {
        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("name", "철학"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CATEGORY_ALREADY_EXISTS"));
    }

    @Test
    @DisplayName("신규 카테고리 생성 실패 - @Valid 검증(400 REQUIRED_FIELD)")
    void create_fail_validation() throws Exception {
        // 빈 문자열
        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("name", ""))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", anyOf(
                        is("REQUIRED_FIELD"), is("INVALID_ARGUMENT"))));

        // 필드 누락
        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of())))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("카테고리 삭제 성공 - 204 No Content")
    void delete_success() throws Exception {
        var created = categoryService.create(new CreateCategoryRequest("IT"));
        Long id = created.getId();
        Assertions.assertTrue(categoryRepository.findById(id).isPresent());

        mockMvc.perform(delete("/api/categories/{categoryId}", id))
                .andExpect(status().isNoContent());

        Assertions.assertTrue(categoryRepository.findById(id).isEmpty());
    }

    @Test
    @DisplayName("카테고리 삭제 실패 - 존재하지 않는 ID(404 CATEGORY_NOT_FOUND)")
    void delete_fail_notFound() throws Exception {
        mockMvc.perform(delete("/api/categories/{categoryId}", 999999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("CATEGORY_NOT_FOUND"));
    }
}