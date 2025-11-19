package com.ex.bookmanagement.dto;

import com.ex.bookmanagement.domain.Book;
import com.ex.bookmanagement.domain.BookCategory;
import com.ex.bookmanagement.domain.BookStatus;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class BookResponse {
    private Long id;
    private String title;
    private String author;
    private BookStatus bookStatus;
    private int stock;
    private List<String> categories;  // 카테고리 이름 목록

    public static BookResponse fromEntity(Book book) {
        return BookResponse.builder()
                .id(book.getId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .bookStatus(book.getBookStatus())
                .stock(book.getStock())
                .categories(
                        book.getBookCategories().stream()
                                .map(BookCategory::getCategory)
                                .map(c -> c.getName())
                                .toList()
                )
                .build();
    }
}
