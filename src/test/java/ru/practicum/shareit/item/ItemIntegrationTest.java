package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingStatus;
import ru.practicum.shareit.exceptions.DataNotFoundException;
import ru.practicum.shareit.item.comments.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoMapper;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class ItemIntegrationTest {
    @Autowired
    private ItemController itemController;
    @Autowired
    private UserController userController;
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private ItemService itemService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ItemDtoMapper itemDtoMapper;
    @Autowired
    private UserDtoMapper userDtoMapper;
    ItemDto itemDto;
    UserDto userDto;
    CommentDto commentDto;
    ItemRequestDto itemRequestDto;
    User user;

    @BeforeEach
    void setup() {
        user = User.builder().id(2L).email("user1@mail.ru").name("name").build();
        userDto = UserDto.builder().id(1L).email("e@mail.ru").name("userName").build();
        commentDto = CommentDto.builder().id(1L).text("comment").authorName(userDto.getName()).created(LocalDate.now()).build();
        itemRequestDto = ItemRequestDto.builder().id(1L).description("description").created(LocalDateTime.now().withNano(0)).build();
        itemDto = ItemDto.builder().id(1L).name("itemName").description("itemDescription").available(true).ownerId(userDto.getId()).comments(List.of(new CommentDto())).build();
    }

    @Test
    void createItem() {
        userController.createUser(userDto);

        itemController.createItem(userDto.getId(), itemDto);

        assertEquals(itemDto.getId(), itemService.getItemById(itemDto.getId(), userDto.getId()).getId());
        assertEquals(itemDto.getName(), itemService.getItemById(itemDto.getId(), userDto.getId()).getName());
    }

    @Test
    void updateItem() {
        userController.createUser(userDto);
        itemController.createItem(userDto.getId(), itemDto);
        itemDto.setName("newItem");

        itemDto = itemController.updateItem(userDto.getId(), itemDto.getId(), itemDto);

        assertEquals("newItem", itemDto.getName());
    }

    @Test
    void updateItemBtNotOwner() {
        userController.createUser(userDto);
        itemController.createItem(userDto.getId(), itemDto);
        itemDto.setName("newItem");
        UserDto newUser = userController.createUser(new UserDto(2L, "email@mail.ru", "name")).getBody();

        assertThrows(DataNotFoundException.class,
                () -> itemController.updateItem(Objects.requireNonNull(newUser).getId(), itemDto.getId(), itemDto));
    }

    @Test
    void getItemById() {
        userController.createUser(userDto);
        itemController.createItem(userDto.getId(), itemDto);
        itemDto.setComments(Collections.emptyList());

        assertEquals(itemDto, itemService.getItemById(itemDto.getId(), userDto.getId()));
    }

    @Test
    void getAllItemsOfUser() {
        userController.createUser(userDto);
        itemController.createItem(userDto.getId(), itemDto);
        itemDto.setComments(Collections.emptyList());
        ItemDto itemDto1 = ItemDto.builder().id(2L).name("newItemDto").description("itemDesc").available(true).ownerId(userDto.getId()).comments(List.of(new CommentDto())).build();
        itemController.createItem(userDto.getId(), itemDto1);
        itemDto1.setComments(Collections.emptyList());

        assertEquals(List.of(itemDto, itemDto1), itemController.getAllItemsOfUser(userDto.getId(), 0, 10));
    }

    @Test
    void searchForItem() {
        userController.createUser(userDto);
        itemController.createItem(userDto.getId(), itemDto);
        itemDto.setComments(List.of(commentDto));

        assertEquals(List.of(itemDto), itemController.searchForItem("cript", 0, 10));
    }

    @Test
    void createCommentToItem() {
        LocalDateTime start = LocalDateTime.now().minusDays(2).withNano(0);
        userController.createUser(userDto);
        userRepository.save(user);
        itemController.createItem(userDto.getId(), itemDto);
        User user = userDtoMapper.dtoToUser(userDto);
        Booking booking = new Booking(1L, start, start.plusHours(1), itemDtoMapper.dtoToItem(itemDto, user), user, BookingStatus.APPROVED);
        bookingRepository.save(booking);

        itemController.createCommentToItem(user.getId(), itemDto.getId(), commentDto);

        assertEquals(List.of(commentDto), itemController.getItemById(itemDto.getId(), userDto.getId()).getComments());
    }
}
