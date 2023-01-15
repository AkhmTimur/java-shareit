package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findAllByBookerIdOrderByIdDesc(Long bookerId);
    Optional<Booking> findByIdAndBookerId(Long bookingId, Long userId);
    List<Booking> findByItemIdAndItemOwnerId(Long itemId, Long userId);
    List<Booking> findByItemIdAndBookerId(Long itemId, Long userId);
    List<Booking> findByItemOwnerId(Long userId);
}

