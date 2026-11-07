package com.smartcharge.booking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * Request payload for cancelling a booking.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingCancelRequest {

    @NotBlank(message = "Cancellation reason is required")
    @Size(min = 5, max = 500, message = "Cancellation reason must be between 5 and 500 characters")
    private String reason;
}
