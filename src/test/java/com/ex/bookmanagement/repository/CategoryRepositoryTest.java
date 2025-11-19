package com.ex.bookmanagement.repository;

import com.ex.bookmanagement.domain.Category;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("CategoryRepository 테스트")
class CategoryRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @BeforeEach
    void init() {
        categoryRepository.save(new Category("철학"));
        categoryRepository.save(new Category("philosophy"));
        categoryRepository.save(new Category("과학"));
    }

    @Test
    @DisplayName("정확히 일치하는 이름 존재 확인")
    void existsByNameIgnoreCase_exactMatch() {
        boolean exists = categoryRepository.existsByNameIgnoreCase("철학");
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("대소문자 무시 확인")
    void existsByNameIgnoreCase_ignoreCase() {
        boolean exists = categoryRepository.existsByNameIgnoreCase("PHILOSOPHY");
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("존재하지 않는 이름")
    void existsByNameIgnoreCase_notExists() {
        boolean exists = categoryRepository.existsByNameIgnoreCase("여행");
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("공백이 포함된 경우 false")
    void existsByNameIgnoreCase_withSpaces() {
        boolean exists = categoryRepository.existsByNameIgnoreCase("  철 학 ");
        assertThat(exists).isFalse();
    }
}
