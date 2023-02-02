package ru.practicum.shareit.item;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {

    List<Item> findByOwnerIdOrderById(Long userId);

    List<Item> findByOwnerIdOrderById(Long userId, PageRequest of);

    @Query("select i from Item i " +
            "where (upper(i.description) like upper(concat('%',?1,'%') ) " +
            "or upper(i.name) like upper(concat('%',?1,'%')))" +
            "and i.available is true")
    List<Item> searchItems(String searchCriteria, PageRequest of);

    List<Item> findByRequestIdIn(List<Long> ids);
}
