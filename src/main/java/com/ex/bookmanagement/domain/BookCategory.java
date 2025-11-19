package com.ex.bookmanagement.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(
        name = "book_category",
        uniqueConstraints = @UniqueConstraint(name = "uk_book_category", columnNames = {"book_id","category_id"})
)
public class BookCategory {
    @Id
    @GeneratedValue
    @Column(name = "book_category_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id")
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    public BookCategory(Book book, Category category) {
        this.book = book;
        this.category = category;
        book.getBookCategories().add(this);
        category.getBookCategories().add(this);
    }

    public void detach() {
        if (book != null) {
            book.getBookCategories().remove(this);
        }

        if (category != null) {
            category.getBookCategories().remove(this);
        }

        this.book = null;
        this.category = null;
    }
}
