package com.ex.bookmanagement.service;

import com.ex.bookmanagement.domain.Category;
import com.ex.bookmanagement.dto.CategoryResponse;
import com.ex.bookmanagement.dto.CreateCategoryRequest;
import com.ex.bookmanagement.exception.BusinessException;
import com.ex.bookmanagement.exception.ErrorCode;
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
 * CategoryService 테스트 클래스
 * 테스트 범위:
 * - 전체 카테고리 조회
 * - 신규 카테고리 등록 (성공, 실패)
 * - 중복 이름 (대소문자/공백 차이 포함)
 * - 카테고리 단권 삭제
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CategoryServiceTest {
    @Autowired private CategoryService categoryService;
    @Autowired private CategoryRepository categoryRepository;

    private Category 철학;
    private Category 예술;
    private Category 역사;

    @BeforeEach
    void init() {
        철학 = categoryRepository.save(new Category("철학"));
        예술 = categoryRepository.save(new Category("예술"));
        역사 = categoryRepository.save(new Category("역사"));
    }

    @Test
    @DisplayName("전체 카테고리 조회 성공")
    void findAllCategories_success() {
        // when
        List<CategoryResponse> list = categoryService.findAllCategories();

        // then
        assertThat(list).hasSize(3);
        assertThat(list).extracting("name")
                .containsExactlyInAnyOrder("철학", "예술", "역사");
    }

    @Test
    @DisplayName("신규 카테고리 등록 성공")
    void create_success() {
        // given
        CreateCategoryRequest req = new CreateCategoryRequest("여행");

        // when
        CategoryResponse res = categoryService.create(req);

        // then
        assertThat(res.getName()).isEqualTo("여행");
        assertThat(categoryRepository.existsByNameIgnoreCase("여행")).isTrue();
    }

    @Test
    @DisplayName("신규 카테고리 등록 실패 - 중복 이름 ")
    void create_fail_duplicateName() {
        // given
        CreateCategoryRequest req = new CreateCategoryRequest("철학"); // 이미 존재

        // when
        BusinessException ex = assertThrows(BusinessException.class,
                () -> categoryService.create(req)
        );

        // then
        assertThat(ex.getCode()).isEqualTo(ErrorCode.CATEGORY_ALREADY_EXISTS);
    }

    @Test
    @DisplayName("신규 카테고리 등록 실패 - 대소문자 또는 공백 차이 중복")
    void create_fail_ignoreCaseAndWhitespace() {
        // given
        CreateCategoryRequest req = new CreateCategoryRequest("  철 학 "); // 공백 포함 → 동일 취급 안 되면 추가 처리 필요

        // when
        BusinessException ex = assertThrows(BusinessException.class,
                () -> categoryService.create(req)
        );

        // then
        assertThat(ex.getCode()).isEqualTo(ErrorCode.CATEGORY_ALREADY_EXISTS);
    }

    @Test
    @DisplayName("카테고리 삭제 성공")
    void delete_success() {
        // given
        CreateCategoryRequest req = new CreateCategoryRequest("IT");
        CategoryResponse created = categoryService.create(req);
        Long id = created.getId();
        assertThat(categoryRepository.findById(id)).isPresent();

        // when
        categoryService.delete(id);

        // then
        assertThat(categoryRepository.findById(id)).isEmpty();
    }

    @Test
    @DisplayName("카테고리 삭제 실패 - 존재하지 않는 ID")
    void delete_fail_notFound() {
        // given
        Long invalidId = 999999L;

        // when
        BusinessException ex = assertThrows(BusinessException.class,
                () -> categoryService.delete(invalidId)
        );

        // then
        assertThat(ex.getCode()).isEqualTo(ErrorCode.CATEGORY_NOT_FOUND);
    }
}