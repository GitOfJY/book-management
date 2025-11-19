package com.ex.bookmanagement.config;

import com.ex.bookmanagement.domain.Book;
import com.ex.bookmanagement.domain.BookStatus;
import com.ex.bookmanagement.domain.Category;
import com.ex.bookmanagement.domain.Rental;
import com.ex.bookmanagement.repository.BookRepository;
import com.ex.bookmanagement.repository.CategoryRepository;
import com.ex.bookmanagement.repository.RentalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Configuration
@RequiredArgsConstructor
@Profile("!test")
public class DataInit {

    @Bean
    @Transactional
    CommandLineRunner initData(BookRepository bookRepository, CategoryRepository categoryRepository, RentalRepository rentalRepository) {
        return args -> {
            Category lit = categoryRepository.save(new Category("문학"));
            Category eco = categoryRepository.save(new Category("경제경영"));
            Category hum = categoryRepository.save(new Category("인문학"));
            Category it  = categoryRepository.save(new Category("IT"));
            Category sci = categoryRepository.save(new Category("과학"));

            List<Book> books = bookRepository.saveAll(List.of(
                    Book.createBook("너에게 해주지 못한 말들", "권태영", List.of(lit), BookStatus.AVAILABLE, 1),
                    Book.createBook("단순하게 배부르게", "현영서", List.of(lit), BookStatus.AVAILABLE, 1),
                    Book.createBook("게으른 사랑", "권태영", List.of(lit), BookStatus.AVAILABLE, 1),
                    Book.createBook("트랜드 코리아 2322", "권태영", List.of(eco), BookStatus.AVAILABLE, 1),
                    Book.createBook("초격자 투자", "장동혁", List.of(eco), BookStatus.AVAILABLE, 1),
                    Book.createBook("파이어족 강환국의 하면 되지 않는다! 퀀트 투자", "홍길동", List.of(eco), BookStatus.AVAILABLE, 1),
                    Book.createBook("진심보다 밥", "이서연", List.of(hum), BookStatus.AVAILABLE, 1),
                    Book.createBook("실패에 대하여 생각하지 마라", "위성원", List.of(hum), BookStatus.AVAILABLE, 1),
                    Book.createBook("실리콘밸리 리더십 쉽다", "지승열", List.of(it), BookStatus.AVAILABLE, 1),
                    Book.createBook("데이터분석을 위한 A 프로그래밍", "지승열", List.of(it), BookStatus.AVAILABLE, 1),
                    Book.createBook("인공지능1-12", "장동혁", List.of(it), BookStatus.AVAILABLE, 1),
                    Book.createBook("-1년차 게임 개발", "위성원", List.of(it), BookStatus.AVAILABLE, 1),
                    Book.createBook("Skye가 알려주는 피부 채색의 비결", "권태영", List.of(it), BookStatus.AVAILABLE, 1),
                    Book.createBook("자연의 발전", "장지명", List.of(sci), BookStatus.AVAILABLE, 1),
                    Book.createBook("코스모스 필 무렵", "이승열", List.of(sci), BookStatus.AVAILABLE, 1)
            ));

            rentalRepository.saveAll(List.of(
                    Rental.create(books.get(0), "김지연"),   // 문학
                    Rental.create(books.get(3), "이도현"),   // 경제경영
                    Rental.create(books.get(6), "박서연"),   // 인문학
                    Rental.create(books.get(8), "정우성"),   // IT
                    Rental.create(books.get(14), "한지민")   // 과학
            ));
        };
    }
}