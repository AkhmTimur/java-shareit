package ru.practicum.shareit.booking.dto;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.exceptions.DataNotFoundException;
import ru.practicum.shareit.item.dto.ItemDtoMapper;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserDto;
import ru.practicum.shareit.user.UserDtoMapper;
import ru.practicum.shareit.user.UserRepository;

@Component
public class BookingDtoMapper {
    private final ItemDtoMapper itemDtoMapper;
    private final UserRepository userRepository;
    private final UserDtoMapper userDtoMapper;

    public BookingDtoMapper(ItemDtoMapper itemDtoMapper, UserRepository userRepository, UserDtoMapper userDtoMapper) {
        this.itemDtoMapper = itemDtoMapper;
        this.userRepository = userRepository;
        this.userDtoMapper = userDtoMapper;
    }

    public BookingDto bookingToDto(Booking booking) {
        UserDto userDto = userDtoMapper.userToDto(booking.getBooker());
        return new BookingDto(
                booking.getId(),
                booking.getStartDate(),
                booking.getEndDate(),
                booking.getItem().getId(),
                itemDtoMapper.itemToDto(booking.getItem()),
                userDto,
                userDto.getId(),
                booking.getStatus()
        );
    }

    public Booking dtoToBooking(BookingDto bookingDto) {
        User user = userRepository.findById(bookingDto.getBooker().getId())
                .orElseThrow(() -> new DataNotFoundException("Пользователь не найден"));
        return new Booking(
                bookingDto.getId(),
                bookingDto.getStart(),
                bookingDto.getEnd(),
                itemDtoMapper.dtoToItem(bookingDto.getItem()),
                user,
                bookingDto.getStatus()
        );
    }

    public BookingDto inDtoToDto(BookingInDto bookingInDto) {
        return new BookingDto(
                bookingInDto.getItemId(),
                bookingInDto.getStart(),
                bookingInDto.getEnd()
        );
    }
}