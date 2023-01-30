package ru.practicum.shareit.bookings.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoMapper;
import ru.practicum.shareit.booking.dto.BookingInDto;
import ru.practicum.shareit.booking.dto.BookingStatus;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserDto;
import ru.practicum.shareit.user.UserDtoMapper;
import ru.practicum.shareit.user.UserRepository;

import java.awt.print.Book;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookingServiceTest {
    @Mock
    private BookingDtoMapper bookingDtoMapper;
    @Mock
    private ItemDtoMapper itemDtoMapper;
    @Mock
    private UserDtoMapper userDtoMapper;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private BookingService bookingService;
    LocalDateTime today = LocalDateTime.now().withNano(0);
    LocalDateTime tomorrow = today.plusDays(1);
    User userToSave = new User(1L, "email@mail.com", "name");
    UserDto userDto = new UserDto(1L, "email@mail.com", "name");
    Item itemToSave = new Item(1L, "name", "description", true, userToSave, null);
    ItemDto itemDto = new ItemDto(1L, "name", "description", true, userToSave.getId(), null, null);
    Booking booking = new Booking(1L, today, tomorrow, itemToSave, userToSave, BookingStatus.APPROVED);
    BookingDto bookingDto = new BookingDto(1L, today, tomorrow, itemToSave.getId(), itemDto, userDto, userDto.getId(), BookingStatus.APPROVED);
    BookingInDto bookingInDto = new BookingInDto(0L, today, tomorrow);

    @BeforeEach
    void setup() {
        lenient().when(userDtoMapper.userToDto(userToSave))
                .thenReturn(new UserDto(userToSave.getId(), userToSave.getEmail(), userToSave.getName()));
        lenient().when(userDtoMapper.dtoToUser(userDto))
                .thenReturn(new User(userDto.getId(), userDto.getEmail(), userDto.getName()));
        lenient().when(itemDtoMapper.itemToDto(itemToSave))
                .thenReturn(new ItemDto(itemToSave.getId(), itemToSave.getName(), itemToSave.getDescription(), itemToSave.getAvailable(), itemToSave.getOwner().getId(), null, null));
        lenient().when(itemDtoMapper.dtoToItem(itemDto, userToSave))
                .thenReturn(new Item(itemDto.getId(), itemDto.getName(), itemDto.getDescription(), itemDto.getAvailable(), userToSave, new ItemRequest()));
        lenient().when(bookingDtoMapper.bookingToDto(booking))
                .thenReturn(new BookingDto(booking.getId(), booking.getStartDate(), booking.getEndDate(), itemToSave.getId(), itemDto, userDto, userDto.getId(), booking.getStatus()));
        lenient().when(bookingDtoMapper.dtoToBooking(bookingDto, userToSave))
                .thenReturn(new Booking(bookingDto.getId(), bookingDto.getStart(), bookingDto.getEnd(), itemToSave, userToSave, BookingStatus.APPROVED));
        lenient().when(bookingDtoMapper.inDtoToDto(bookingInDto))
                .thenReturn(new BookingDto(bookingInDto.getItemId(), bookingInDto.getStart(), bookingInDto.getEnd()));
    }

    @Test
    void createBooking_itemFoundAndAvailableAndUserFound_thenCreateBooking() {
        Long itemId = 0L;
        Long userId = 0L;
        BookingDto bookingDto = new BookingDto(1L, null, null, itemId, new ItemDto(), new UserDto(userId, null, null), 1L, null);
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(itemToSave));
        when(userRepository.findById(userId)).thenReturn(Optional.of(userToSave));
        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));

        BookingDto bookingDto1 = bookingService.createBooking(new BookingInDto(itemId, null, null), userId);


        verify(bookingRepository).save(new Booking());
    }
}
