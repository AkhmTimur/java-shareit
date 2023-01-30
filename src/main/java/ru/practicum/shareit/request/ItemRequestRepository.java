package ru.practicum.shareit.request;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.List;

public interface ItemRequestRepository extends JpaRepository<ItemRequest, Long> {
    List<ItemRequest> findByRequesterId(Long userId);

    @Query(value = "select * from item_request where requester_id <> ?", nativeQuery = true)
    List<ItemRequest> findAllByRequesterIdIsNot(Long userId);
}
