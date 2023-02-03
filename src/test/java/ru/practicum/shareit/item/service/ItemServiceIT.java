package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.webservices.client.WebServiceClientTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoMapper;
import ru.practicum.shareit.booking.dto.BookingStatus;
import ru.practicum.shareit.exceptions.DataNotFoundException;
import ru.practicum.shareit.exceptions.IncorrectDataException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.comments.CommentRepository;
import ru.practicum.shareit.item.comments.dto.CommentDto;
import ru.practicum.shareit.item.comments.dto.CommentDtoMapper;
import ru.practicum.shareit.item.comments.model.Comment;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@WebServiceClientTest(ItemService.class)
class ItemServiceIT {
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
    private ItemDtoMapper itemDtoMapper;
    @MockBean
    private BookingDtoMapper bookingDtoMapper;
    @MockBean
    private CommentDtoMapper commentDtoMapper;
    private ItemDto itemDto;
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

        itemDto = new ItemDto(
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

    @Test
    void getAllItemsOfUser() {
        Item item = new Item();
        item.setId(1L);
        item.setOwner(user2);
        when(itemRepository.findByOwnerIdOrderById(anyLong(), any(PageRequest.class))).thenReturn(List.of(item));
        when(bookingRepository.findByItemIdIn(List.of(anyLong()))).thenReturn(List.of(booking1));
        when(commentRepository.findByItemIdIn(List.of(anyLong()))).thenReturn(Collections.emptyList());
        when(itemDtoMapper.itemToDto(any(Item.class))).thenReturn(itemDto);
        when(bookingDtoMapper.bookingToDto(any(Booking.class))).thenReturn(new BookingDto());
        List<ItemDto> itemInfoDtoList = itemService.getAllItemsOfUser(user2.getId(), 0, 10);

        assertEquals(itemInfoDtoList.size(), 1);
    }

    @Test
    void addNewItem() {
        Item item = new Item();
        item.setId(1L);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user1));
        when(itemDtoMapper.itemToDto(any(Item.class))).thenReturn(itemDto);
        when(itemDtoMapper.dtoToItem(any(ItemDto.class), any(User.class))).thenReturn(item);

        when(itemRepository.save(any(Item.class))).thenReturn(item);

        assertEquals(itemService.createItem(itemDto).getId(), 1L);
    }

    @Test
    void updateItem() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user1));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item1));
        when(itemDtoMapper.itemToDto(any(Item.class))).thenReturn(itemDto);
        when(itemDtoMapper.dtoToItem(any(ItemDto.class), any(User.class))).thenReturn(item1);
        when(itemRepository.save(any(Item.class))).thenReturn(item1);
        ItemDto newDto = new ItemDto();
        assertThrows(DataNotFoundException.class, () -> itemService.updateItem(newDto));

        itemDto.setName("test");
        itemDto.setDescription("description");
        itemDto.setAvailable(false);
        assertNotNull(itemService.updateItem(itemDto));
    }

    @Test
    void getItemById() {
        when(itemRepository.findById(1L)).thenAnswer(i -> {
            Item item = new Item();
            item.setOwner(user2);
            item.setId(1L);
            return Optional.of(item);
        });
        when(commentRepository.findByItemId(anyLong())).thenAnswer(i -> {
            Comment comment = new Comment();
            comment.setAuthor(user2);
            return List.of(comment, comment);
        });
        Booking booking2 = Booking.builder()
                .id(1L)
                .startDate(LocalDateTime.of(2023, 2, 10, 12, 0))
                .endDate(LocalDateTime.of(2023, 3, 10, 12, 0))
                .item(item1)
                .booker(user1)
                .status(BookingStatus.WAITING)
                .build();
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(new User()));
        List<Booking> bookingList = List.of(booking1, booking2);

        when(bookingRepository.findByItemIdAndItemOwnerId(anyLong(), anyLong())).thenReturn(bookingList);
        when(itemDtoMapper.itemToDto(any(Item.class))).thenReturn(itemDto);
        when(commentDtoMapper.commentToDto(any(Comment.class))).thenReturn(commentDto);
        when(bookingDtoMapper.bookingToDto(any(Booking.class))).thenReturn(new BookingDto());

        assertThrows(DataNotFoundException.class, () -> itemService.getItemById(2L, 2L));

        ItemDto itemDto = itemService.getItemById(1L, 2L);

        assertNotNull(itemDto.getLastBooking());
        assertNotNull(itemDto.getNextBooking());
    }

    @Test
    void searchItems() {
        Item item = new Item();
        item.setName("item name");
        item.setDescription("test");
        item.setAvailable(true);
        when(itemRepository.searchItems(anyString(), any(PageRequest.class))).thenReturn(Collections.emptyList());

        assertEquals(itemService.searchForItem("something", 0, 10).size(), 0);

        when(itemRepository.searchItems(anyString(), any(PageRequest.class))).thenReturn(List.of(item));

        assertEquals(itemService.searchForItem("test", 0, 10).size(), 1);
    }

    @Test
    void createCommentToItem() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(new User()));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(new Item()));
        when(commentRepository.save(any(Comment.class))).thenReturn(new Comment());
        booking1.setStatus(BookingStatus.APPROVED);
        when(bookingRepository.findByItemIdAndBookerId(anyLong(), anyLong())).thenReturn(List.of(booking1));
        when(commentRepository.findByItemIdAndAuthorId(anyLong(), anyLong())).thenReturn(Optional.of(new Comment()));
        when(commentDtoMapper.commentToDto(any(Comment.class))).thenReturn(commentDto);
        when(userRepository.getReferenceById(anyLong())).thenReturn(user1);
        Comment comment = new Comment("text", item1, user1);
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);
        Item item = new Item();
        item.setId(1L);
        item.setName("item name");
        item.setDescription("test description");
        item.setAvailable(true);
        when(itemRepository.getReferenceById(anyLong())).thenReturn(item);
        assertThrows(IncorrectDataException.class, () -> itemService.createCommentToItem(1L, "text", 1L));
        when(bookingRepository.findByItemIdAndBookerId(anyLong(), anyLong())).thenReturn(List.of(booking1));

        assertNotNull(itemService.createCommentToItem(1L, "text", 1L));
    }
}