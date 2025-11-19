package com.ex.bookmanagement.domain;

import com.ex.bookmanagement.exception.BusinessException;
import com.ex.bookmanagement.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.*;
import java.util.stream.Collectors;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Book {
    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false, length = 80)
    private String author;

    @Column(nullable = false, length = 120)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private BookStatus bookStatus;

    @Column(nullable = false)
    private int stock;

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Rental> rentals = new ArrayList<>();

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BookCategory> bookCategories = new ArrayList<>();

    @Builder
    private Book(String title, String author, BookStatus bookStatus, int stock) {
        if (stock < 0) {
            throw new BusinessException(ErrorCode.INVALID_STOCK_QUANTITY);
        }
        this.title = title;
        this.author = author;
        this.bookStatus = (bookStatus == null ? BookStatus.AVAILABLE : bookStatus);
        this.stock = stock;
    }

    public static Book createBook(
            String title,
            String author,
            List<Category> categories,
            BookStatus initialStatus,
            int initialStock
    ) {
        if (title == null || title.isBlank()) {
            throw new BusinessException(ErrorCode.REQUIRED_FIELD, Map.of("field", "제목"));
        }
        if (author == null || author.isBlank()) {
            throw new BusinessException(ErrorCode.REQUIRED_FIELD, Map.of("field", "저자"));
        }
        if (categories == null || categories.isEmpty()) {
            throw new BusinessException(ErrorCode.CATEGORY_REQUIRED);
        }
        if (initialStock < 0) {
            throw new BusinessException(ErrorCode.INVALID_STOCK_QUANTITY);
        }

        BookStatus status = (initialStatus == null) ? BookStatus.AVAILABLE : initialStatus;

        // 엔티티 생성
        Book book = Book.builder()
                .title(title)
                .author(author)
                .bookStatus(initialStatus == null ? BookStatus.AVAILABLE : initialStatus)
                .stock(initialStock)
                .build();

        // 연관관계 설정(양방향 동기화) > 중복 카테고리 방지, distinct 처리
        categories.stream().distinct().forEach(book::addCategory);
        return book;
    }

    /** 재고 증가 */
    public void increaseStock(int qty) {
        if (qty < 1) {
            throw new BusinessException(ErrorCode.STOCK_INCREASE_AMOUNT_INVALID);
        }
        this.stock += qty;
    }

    /** 재고 감소 */
    public void decreaseStock(int qty) {
        if (qty < 1) {
            throw new BusinessException(ErrorCode.STOCK_DECREASE_AMOUNT_INVALID);
        }
        if (this.stock < qty) {
            throw new BusinessException(ErrorCode.OUT_OF_STOCK);
        }
        this.stock -= qty;
    }

    // 도서 상태 변경
    public void changeStatus(BookStatus newStatus) {
        if (newStatus == null) {
            throw new BusinessException(ErrorCode.BOOK_STATUS_NULL);
        }
        this.bookStatus = newStatus;
    }

    // 신규 카테고리 추가
    public void addCategory(Category category) {
        boolean exists = bookCategories.stream()
                .anyMatch(bc -> bc.getCategory().equals(category));
        if (exists) {
            return;
        }
        new BookCategory(this, category);
    }

    public boolean hasCategory(Category category) {
        return bookCategories.stream()
                .anyMatch(bc -> bc.getCategory().equals(category));
    }

    /** 카테고리 변경: old 리스트 -> new 리스트 */
    public void changeCategories(Collection<Category> newCategories) {
        // 대상 ID 집합 (중복 제거)
        Set<Long> targetIds = newCategories.stream()
                .map(Category::getId)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        // 현재 연결된 카테고리 ID 집합
        Set<Long> currentIds = this.bookCategories.stream()
                .map(bc -> bc.getCategory().getId())
                .collect(Collectors.toSet());

        // 제거 카테고리
        Set<Long> toRemove = new HashSet<>(currentIds);
        toRemove.removeAll(targetIds);

        // 추가 카테고리
        Set<Long> toAdd = new HashSet<>(targetIds);
        toAdd.removeAll(currentIds);

        // 3) 제거 — 스냅샷 + detach만 호출
        if (!toRemove.isEmpty()) {
            List<BookCategory> snapshot = new ArrayList<>(this.bookCategories);
            for (BookCategory bc : snapshot) {
                if (toRemove.contains(bc.getCategory().getId())) {
                    bc.detach(); // 양방향 컬렉션에서 제거
                }
            }
        }

        // 4) 추가 (양방향 생성자 활용)
        if (!toAdd.isEmpty()) {
            Map<Long, Category> mapById = newCategories.stream()
                    .collect(Collectors.toMap(Category::getId, c -> c, (a,b) -> a));
            for (Long cid : toAdd) {
                new BookCategory(this, mapById.get(cid));
            }
        }
    }
}
