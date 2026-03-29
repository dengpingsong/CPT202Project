package com.cpt202.service;

import com.cpt202.model.Item;
import com.cpt202.repository.ItemRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service layer for {@link Item} business logic.
 */
@Service
public class ItemService {

    private final ItemRepository itemRepository;

    public ItemService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    /** Return all items. */
    public List<Item> findAll() {
        return itemRepository.findAll();
    }

    /** Return a single item by id, or empty if not found. */
    public Optional<Item> findById(Long id) {
        return itemRepository.findById(id);
    }

    /** Persist a new or updated item. */
    public Item save(Item item) {
        return itemRepository.save(item);
    }

    /** Delete an item by id. */
    public void deleteById(Long id) {
        itemRepository.deleteById(id);
    }
}
