package com.ex.bookmanagement.dto;

import com.ex.bookmanagement.domain.Rental;
import com.ex.bookmanagement.domain.RentalStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class RentResponse {
    private Long rentalId;

    private Long bookId;

    private String renterName;

    private RentalStatus status;

    private LocalDateTime rentedAt;

    private LocalDateTime returnedAt;

    public static RentResponse fromEntity(Rental rental) {
        return new RentResponse(
                rental.getId(),
                rental.getBook().getId(),
                rental.getRenterName(),
                rental.getRentalStatus(),
                rental.getRentedDate(),
                rental.getDueDate()
        );
    }
}
