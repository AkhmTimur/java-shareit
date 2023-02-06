package ru.practicum.shareit.booking.dto;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.item.dto.ItemDtoMapper;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserDto;
import ru.practicum.shareit.user.UserDtoMapper;

@Component
public class BookingDtoMapper {
    private final ItemDtoMapper itemDtoMapper;
    private final UserDtoMapper userDtoMapper;

    public BookingDtoMapper(ItemDtoMapper itemDtoMapper, UserDtoMapper userDtoMapper) {
        this.itemDtoMapper = itemDtoMapper;
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

    public Booking dtoToBooking(BookingDto bookingDto, User user) {
        return new Booking(
                bookingDto.getId(),
                bookingDto.getStart(),
                bookingDto.getEnd(),
                itemDtoMapper.dtoToItem(bookingDto.getItem(), user),
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