package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingInDto;
import ru.practicum.shareit.booking.dto.BookingStatus;
import ru.practicum.shareit.exceptions.IncorrectDataException;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@Controller
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
@Validated
public class BookingController {
	private final BookingClient bookingClient;

	@PostMapping
	public ResponseEntity<Object> createBooking(@RequestHeader("X-Sharer-User-Id") Long userId,
												@RequestBody BookingInDto bookingInDto) {
		return bookingClient.createBooking(userId, bookingInDto);
	}

	@PatchMapping("/{bookingId}")
	public ResponseEntity<Object> updateBooking(@RequestHeader("X-Sharer-User-Id") Long userId,
												@PathVariable("bookingId") Long bookingId,
												@RequestParam("approved") boolean approvedType) {
		return bookingClient.updateBooking(userId, bookingId, approvedType);
	}

	@GetMapping("/{bookingId}")
	public ResponseEntity<Object> getBooking(@RequestHeader("X-Sharer-User-Id") long userId,
											 @PathVariable Long bookingId) {
		log.info("Get booking {}, userId={}", bookingId, userId);
		return bookingClient.getBooking(userId, bookingId);
	}

	@GetMapping
	public ResponseEntity<Object> getAllBookings(@RequestHeader("X-Sharer-User-Id") long userId,
			@RequestParam(name = "state", defaultValue = "all") String stateParam,
			@PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
			@Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
		String state;
		if(stateParam.equals("all")) {
			state = "ALL";
		} else {
			state = BookingStatus.from(stateParam)
					.orElseThrow(() -> new IncorrectDataException("Unknown state: " + stateParam));
		}
		log.info("Get booking with state {}, userId={}, from={}, size={}", stateParam, userId, from, size);
		return bookingClient.getAllBookings(userId, state, from, size);
	}

	@GetMapping("/owner")
	public ResponseEntity<Object> getAllBookingsOwner(@RequestHeader("X-Sharer-User-Id") long userId,
												 @RequestParam(name = "state", defaultValue = "all") String stateParam,
												 @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
												 @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
		String state;
		if(stateParam.equals("all")) {
			state = "ALL";
		} else {
			state = BookingStatus.from(stateParam)
					.orElseThrow(() -> new IncorrectDataException("Unknown state: " + stateParam));
		}
		log.info("Get booking with state {}, userId={}, from={}, size={}", stateParam, userId, from, size);
		return bookingClient.getAllBookingsOwner(userId, state, from, size);
	}
}
