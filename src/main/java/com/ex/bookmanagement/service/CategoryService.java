package com.ex.bookmanagement.service;

import com.ex.bookmanagement.domain.Category;
import com.ex.bookmanagement.dto.CategoryResponse;
import com.ex.bookmanagement.dto.CreateCategoryRequest;
import com.ex.bookmanagement.exception.BusinessException;
import com.ex.bookmanagement.exception.ErrorCode;
import com.ex.bookmanagement.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly=true)
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;

    /** 전체 조회 */
    public List<CategoryResponse> findAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        return categories.stream()
                .map(CategoryResponse::fromEntity)
                .toList();
    }

    /** 신규 카테고리 생성 (중복 이름 방지, 대소문자 무시) */
    @Transactional
    public CategoryResponse create(CreateCategoryRequest req) {
        String normalizedName = req.getName()
                .replaceAll("\\s+", "")   // 모든 공백 제거
                .trim()
                .toLowerCase();

        boolean exists = categoryRepository.findAll().stream()
                .map(Category::getName)
                .map(name -> name.replaceAll("\\s+", "").trim().toLowerCase())
                .anyMatch(existing -> existing.equals(normalizedName));

        if (exists) {
            throw new BusinessException(ErrorCode.CATEGORY_ALREADY_EXISTS, Map.of("name", normalizedName));
        }

        Category saved = categoryRepository.save(new Category(req.getName().trim()));
        return CategoryResponse.fromEntity(saved);
    }

    /** 카테고리 삭제 */
    @Transactional
    public void delete(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND, Map.of("id", categoryId)));
        // (Category / BookCategory) : orphanRemoval=true 로 매핑 자동 삭제
        categoryRepository.delete(category);
    }
}
