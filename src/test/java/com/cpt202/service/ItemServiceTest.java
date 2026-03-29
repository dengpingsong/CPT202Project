package com.cpt202.service;

import com.cpt202.model.Item;
import com.cpt202.repository.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link ItemService} using Mockito to mock the repository.
 */
@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private ItemService itemService;

    private Item sampleItem;

    @BeforeEach
    void setUp() {
        sampleItem = new Item("Widget", "A sample widget");
        sampleItem.setId(1L);
    }

    @Test
    @DisplayName("findAll returns all items from the repository")
    void findAll_returnsAllItems() {
        when(itemRepository.findAll()).thenReturn(List.of(sampleItem));

        List<Item> result = itemService.findAll();

        assertThat(result).hasSize(1).containsExactly(sampleItem);
        verify(itemRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("findById returns item when it exists")
    void findById_whenExists_returnsItem() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(sampleItem));

        Optional<Item> result = itemService.findById(1L);

        assertThat(result).isPresent().contains(sampleItem);
    }

    @Test
    @DisplayName("findById returns empty when item does not exist")
    void findById_whenNotFound_returnsEmpty() {
        when(itemRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Item> result = itemService.findById(99L);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("save delegates to the repository and returns saved item")
    void save_delegatesToRepository() {
        when(itemRepository.save(sampleItem)).thenReturn(sampleItem);

        Item result = itemService.save(sampleItem);

        assertThat(result).isEqualTo(sampleItem);
        verify(itemRepository).save(sampleItem);
    }

    @Test
    @DisplayName("deleteById delegates to the repository")
    void deleteById_delegatesToRepository() {
        itemService.deleteById(1L);
        verify(itemRepository).deleteById(1L);
    }
}
