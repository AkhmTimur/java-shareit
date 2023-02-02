package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.webservices.client.WebServiceClientTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingStatus;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.comments.CommentRepository;
import ru.practicum.shareit.item.comments.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.UserService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@WebServiceClientTest(ItemService.class)
class ItemServiceImplTest {
    @Autowired
    private ItemService itemService;
    @MockBean
    private ItemRepository itemRepository;
    @MockBean
    private CommentRepository commentRepository;
    @MockBean
    private BookingRepository bookingRepository;
    @MockBean
    private UserRepository userRepository;
    @MockBean
    private ItemRequestRepository itemRequestRepository;
    @MockBean
    private UserService userService;

    private ItemDto itemDtoOther;

    private CommentDto commentDto;
    private ItemRequest itemRequestOne;
    private User user1;
    private User user2;
    private Booking booking1;
    Item item1;

    @BeforeEach
    void beforeEach() {
        user1 = new User(1L, "user1@email", "name1");
        user2 = new User(2L, "user2@email", "name2");
        itemRequestOne = new ItemRequest(1L, "item1", user1, LocalDateTime.now());

        itemDtoOther = new ItemDto(
                1L,
                "name",
                "description",
                true,
                user1.getId(),
                List.of(new CommentDto(8L, "text", user2.getName(), LocalDate.of(2022, 1, 1))),
                itemRequestOne.getRequester().getId()
        );

        commentDto = new CommentDto(
                1L,
                "something",
                "userName",
                LocalDate.now()
        );

        item1 = new Item(1L, "name", "description", true, user1, null);
        booking1 = Booking.builder()
                .id(1L)
                .startDate(LocalDateTime.of(2023, 1, 10, 12, 0))
                .endDate(LocalDateTime.of(2023, 2, 10, 12, 0))
                .item(item1)
                .booker(user1)
                .status(BookingStatus.WAITING)
                .build();
    }
}