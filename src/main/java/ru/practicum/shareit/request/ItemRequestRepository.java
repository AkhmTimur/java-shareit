package ru.practicum.shareit.request;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.User;

import java.util.List;

public interface ItemRequestRepository extends JpaRepository<ItemRequest, Long> {
    List<ItemRequest> findByRequesterId(Long userId);

    @Query("select ir from ItemRequest ir where ir.requester <> ?1")
    List<ItemRequest> findAllByRequesterIdIsNot(User user);
}
