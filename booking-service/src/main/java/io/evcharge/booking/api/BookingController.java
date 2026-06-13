package io.evcharge.booking.api;

import io.evcharge.booking.api.dto.BookingRequest;
import io.evcharge.booking.api.dto.BookingResponse;
import io.evcharge.booking.service.BookingService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@Tag(name = "Bookings")
public class BookingController {

    private final BookingService bookingService;

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse create(@Valid @RequestBody BookingRequest req,
                                  @RequestHeader("X-User-Id") Long userId,
                                  @RequestHeader("X-User-Email") String userEmail) {
        return bookingService.create(req, userId, userEmail);
    }

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping
    public Page<BookingResponse> myBookings(@RequestHeader("X-User-Id") Long userId, Pageable pg) {
        return bookingService.listForUser(userId, pg);
    }

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping("/{id}")
    public BookingResponse get(@PathVariable Long id, @RequestHeader("X-User-Id") Long userId) {
        return bookingService.get(id, userId);
    }

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @PostMapping("/{id}/cancel")
    public BookingResponse cancel(@PathVariable Long id, @RequestHeader("X-User-Id") Long userId) {
        return bookingService.cancel(id, userId);
    }
}
