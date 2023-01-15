package ru.practicum.shareit.booking.dto;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.item.dto.ItemDtoMapper;
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
        return new Booking(
                bookingDto.getId(),
                bookingDto.getStart(),
                bookingDto.getEnd(),
                itemDtoMapper.dtoToItem(bookingDto.getItem()),
                userRepository.findById(bookingDto.getBooker().getId()).get(),
                bookingDto.getStatus()
        );
    }
}