package com.cpt202.controller;

import com.cpt202.model.ApiResponse;
import com.cpt202.model.Item;
import com.cpt202.service.ItemService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * REST controller that exposes CRUD endpoints for {@link Item} resources.
 *
 * <pre>
 * GET    /api/items       – list all items
 * GET    /api/items/{id}  – get one item
 * POST   /api/items       – create an item
 * PUT    /api/items/{id}  – update an item
 * DELETE /api/items/{id}  – delete an item
 * </pre>
 */
@RestController
@RequestMapping("/api/items")
public class ItemController {

    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping
    public ApiResponse<List<Item>> listAll() {
        return ApiResponse.success(itemService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Item>> getById(@PathVariable Long id) {
        Optional<Item> item = itemService.findById(id);
        return item.map(value -> ResponseEntity.ok(ApiResponse.success(value)))
                   .orElseGet(() -> ResponseEntity
                           .status(HttpStatus.NOT_FOUND)
                           .body(ApiResponse.error(404, "Item not found")));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Item>> create(@RequestBody Item item) {
        Item saved = itemService.save(item);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Item>> update(@PathVariable Long id,
                                                     @RequestBody Item item) {
        if (itemService.findById(id).isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(404, "Item not found"));
        }
        item.setId(id);
        return ResponseEntity.ok(ApiResponse.success(itemService.save(item)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        if (itemService.findById(id).isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(404, "Item not found"));
        }
        itemService.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
