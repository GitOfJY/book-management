package com.ex.bookmanagement.service;

import com.ex.bookmanagement.domain.Book;
import com.ex.bookmanagement.domain.Category;
import com.ex.bookmanagement.dto.BookResponse;
import com.ex.bookmanagement.dto.ChangeBookStatusRequest;
import com.ex.bookmanagement.dto.CreateBookRequest;
import com.ex.bookmanagement.exception.BusinessException;
import com.ex.bookmanagement.exception.ErrorCode;
import com.ex.bookmanagement.repository.BookRepository;
import com.ex.bookmanagement.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly=true)
@RequiredArgsConstructor
public class BookService {
    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;

    /** 도서 전체 목록 조회 */
    public List<BookResponse> findAllBooks() {
        return bookRepository.findAll()
                .stream()
                .map(BookResponse::fromEntity)
                .toList();
    }

    /** 신규 도서 등록 */
    @Transactional
    public Long create(CreateBookRequest dto){
        // 1) 카테고리 확인
        List<Long> ids = dto.getCategoryIds();
        List<Category> categories = categoryRepository.findAllById(ids);
        if (categories.size() != ids.size()) {
            // 1-1) 실제 존재하는 ID 목록
            Set<Long> foundIds = categories.stream()
                    .map(Category::getId)
                    .collect(Collectors.toSet());

            // 1-2) 요청했지만 존재하지 않는 ID 목록
            List<Long> missingIds = ids.stream()
                    .filter(id -> !foundIds.contains(id))
                    .toList();

            if (!missingIds.isEmpty()) {
                throw new BusinessException(
                        ErrorCode.CATEGORY_NOT_FOUND,
                        Map.of("ids", missingIds)
                );
            }
        }

        // 2) 동일 제목+저자 도서 존재 여부 확인
        Optional<Book> existingOpt = bookRepository
                .findByTitleAndAuthor(dto.getTitle(), dto.getAuthor());
        if (existingOpt.isPresent()) {
            Book existing = existingOpt.get();
            existing.increaseStock(dto.getStock() > 0 ? dto.getStock() : 1);
            return existing.getId();
        }

        // 3)
        Book book = Book.createBook(dto.getTitle(), dto.getAuthor(), categories, dto.getBookStatus(), dto.getStock());

        // 4) 저장
        return bookRepository.save(book).getId();
    }

    /** 카테고리 변경 (set-diff 방식) */
    @Transactional
    public void updateCategories(Long bookId, List<Long> newCategoryIds) {
        // 1) 책 + 현재 매핑까지 로드
        Book book = bookRepository.findByIdWithCategories(bookId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BOOK_NOT_FOUND, Map.of("id", bookId)));

        // 2) 요청 카테고리 조회 & 유효성 검증 (중복 제거)
        List<Long> distinctIds = new ArrayList<>(new LinkedHashSet<>(newCategoryIds));
        List<Category> targets = categoryRepository.findAllById(distinctIds);
        if (targets.size() != distinctIds.size()) {
            throw new BusinessException(ErrorCode.CATEGORY_NOT_FOUND, Map.of("ids", distinctIds));
        }

        // 3) 실제 동기화는 도메인에 위임
        book.changeCategories(targets);
    }

    /** 도서 상태 변경 */
    @Transactional
    public void changeStatus(Long bookId, ChangeBookStatusRequest req) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BOOK_NOT_FOUND, Map.of("id", bookId)));

        if (book.getBookStatus() == req.getStatus()) return;
        book.changeStatus(req.getStatus());
    }

    /** 저자 또는 제목으로 도서 검색 */
    public List<BookResponse> searchByAuthorAndTitle(String author, String title, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        Page<Book> books = bookRepository.searchByAuthorAndTitle(author, title, pageable);
        return books.stream()
                .map(BookResponse::fromEntity)
                .toList();
    }

    /** 카테고리 별 도서 검색 */
    public List<BookResponse> searchByCategory(Long categoryId, String categoryName, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        Page<Book> books = bookRepository.searchByCategory(categoryId, categoryName, pageable);
        return books.stream()
                .map(BookResponse::fromEntity)
                .toList();
    }

    /** 도서 삭제 */
    @Transactional
    public void delete(Long bookId) {
        Book book = bookRepository.findByIdWithCategories(bookId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BOOK_NOT_FOUND, Map.of("id", bookId)));
        bookRepository.deleteById(bookId);
    }
}
