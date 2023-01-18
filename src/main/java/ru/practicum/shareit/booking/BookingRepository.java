package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.booking.dto.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findAllByBookerIdOrderByIdDesc(Long bookerId);

    List<Booking> findAllByBookerIdAndStatusOrderByIdDesc(Long bookerId, BookingStatus status);

    List<Booking> findAllByBookerIdAndStartDateAfterOrderByIdDesc(Long bookerId, LocalDateTime currentDateTime);

    List<Booking> findAllByBookerIdAndEndDateBeforeOrderByIdDesc(Long bookerId, LocalDateTime currentDateTime);

    List<Booking> findAllByBookerIdAndEndDateAfterAndStartDateBeforeOrderByIdDesc(Long bookerId, LocalDateTime currentDateTime, LocalDateTime currentDateTime1);

    List<Booking> findAllByItemOwnerIdAndStartDateAfterOrderByIdDesc(Long bookerId, LocalDateTime currentDateTime);

    List<Booking> findAllByItemOwnerIdAndEndDateBeforeOrderByIdDesc(Long bookerId, LocalDateTime currentDateTime);

    List<Booking> findAllByItemOwnerIdAndEndDateAfterAndStartDateBeforeOrderByIdDesc(Long bookerId, LocalDateTime currentDateTime, LocalDateTime currentDateTime1);

    List<Booking> findByItemIdAndItemOwnerId(Long itemId, Long userId);

    List<Booking> findByItemIdAndBookerId(Long itemId, Long userId);

    List<Booking> findByItemOwnerIdOrderByIdDesc(Long userId);

    List<Booking> findByItemOwnerIdAndStatusOrderByIdDesc(Long userId, BookingStatus status);

    List<Booking> findByItemIdIn(List<Long> ids);

    List<Booking> findByItemId(Long id);
}

