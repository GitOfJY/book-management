package com.ex.bookmanagement.repository;

import com.ex.bookmanagement.domain.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {
    /**  저자 또는 제목으로 도서 검색 */
    @Query("""
        select b
        from Book b
        where (:author is null or lower(b.author) like lower(concat('%', :author, '%')))
          and (:title  is null or lower(b.title)  like lower(concat('%', :title,  '%')))
        """)
    Page<Book> searchByAuthorAndTitle(
                        @Param("author") String author,
                           @Param("title") String title,
                           Pageable pageable);

    /** 카테고리 별 도서 검색 */
    @Query("""
    select distinct b
    from Book b
      join b.bookCategories bc
      join bc.category c
    where (:categoryId is null or c.id = :categoryId)
      and (:categoryName is null or lower(c.name) like lower(concat('%', :categoryName, '%')))
    """)
    Page<Book> searchByCategory(@Param("categoryId") Long categoryId,
                                @Param("categoryName") String categoryName,
                                Pageable pageable);

    @Query("""
      select b
      from Book b
      left join fetch b.bookCategories bc
      left join fetch bc.category c
      where b.id = :id
    """)
    Optional<Book> findByIdWithCategories(@Param("id") Long id);

    Optional<Book> findByTitleAndAuthor(String title, String author);
}
