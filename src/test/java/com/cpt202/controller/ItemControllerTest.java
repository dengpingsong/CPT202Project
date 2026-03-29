package com.cpt202.controller;

import com.cpt202.model.Item;
import com.cpt202.service.ItemService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Slice tests for {@link ItemController} using {@code @WebMvcTest}.
 * Covers the full CRUD surface area via MockMvc.
 */
@WebMvcTest(ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemService itemService;

    private Item item;

    @BeforeEach
    void setUp() {
        item = new Item("Widget", "A sample widget");
        item.setId(1L);
    }

    @Test
    @DisplayName("GET /api/items returns list of items")
    void listAll_returnsItems() throws Exception {
        when(itemService.findAll()).thenReturn(List.of(item));

        mockMvc.perform(get("/api/items"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.status").value(200))
               .andExpect(jsonPath("$.data[0].name").value("Widget"));
    }

    @Test
    @DisplayName("GET /api/items/{id} returns item when found")
    void getById_whenFound_returnsItem() throws Exception {
        when(itemService.findById(1L)).thenReturn(Optional.of(item));

        mockMvc.perform(get("/api/items/1"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.data.name").value("Widget"));
    }

    @Test
    @DisplayName("GET /api/items/{id} returns 404 when not found")
    void getById_whenNotFound_returns404() throws Exception {
        when(itemService.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/items/99"))
               .andExpect(status().isNotFound())
               .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("POST /api/items creates and returns item")
    void create_returnsCreatedItem() throws Exception {
        when(itemService.save(any(Item.class))).thenReturn(item);

        mockMvc.perform(post("/api/items")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(objectMapper.writeValueAsString(item)))
               .andExpect(status().isCreated())
               .andExpect(jsonPath("$.data.name").value("Widget"));
    }

    @Test
    @DisplayName("PUT /api/items/{id} updates and returns item")
    void update_whenFound_returnsUpdatedItem() throws Exception {
        when(itemService.findById(1L)).thenReturn(Optional.of(item));
        when(itemService.save(any(Item.class))).thenReturn(item);

        mockMvc.perform(put("/api/items/1")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(objectMapper.writeValueAsString(item)))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.data.name").value("Widget"));
    }

    @Test
    @DisplayName("DELETE /api/items/{id} deletes item when found")
    void delete_whenFound_returnsOk() throws Exception {
        when(itemService.findById(1L)).thenReturn(Optional.of(item));
        doNothing().when(itemService).deleteById(1L);

        mockMvc.perform(delete("/api/items/1"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    @DisplayName("DELETE /api/items/{id} returns 404 when not found")
    void delete_whenNotFound_returns404() throws Exception {
        when(itemService.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/items/99"))
               .andExpect(status().isNotFound());
    }
}
