package com.cpt202.repository;

import com.cpt202.model.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for {@link Item} entities.
 * Provides standard CRUD operations out of the box.
 */
@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
}
