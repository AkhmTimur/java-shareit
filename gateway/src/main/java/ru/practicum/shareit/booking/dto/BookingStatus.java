package ru.practicum.shareit.booking.dto;

import java.util.Optional;

public enum BookingStatus {
    WAITING, APPROVED, REJECTED, PAST, FUTURE, CURRENT, ALL, CANCELED;

    public static Optional<String> from(String stringState) {
        for (BookingStatus state : values()) {
            if (state.name().equalsIgnoreCase(stringState)) {
                return Optional.of(state.toString());
            }
        }
        return Optional.empty();
    }
}
