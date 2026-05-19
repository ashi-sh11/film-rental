package com.example.backend.service;

import com.example.backend.dto.InventoryDto;
import com.example.backend.entity.*;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.repository.*;
import com.example.backend.service.inventory.InventoryService;
import com.example.backend.util.AuthUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private FilmRepository filmRepository;

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private AuthUtil authUtil;

    @Mock
    private RentalRepository rentalRepository;

    @Mock
    private StaffRepository staffRepository;

    @InjectMocks
    private InventoryService inventoryService;

    private Staff staff;
    private Film film;

    @BeforeEach
    void setUp() {
        Store store = new Store();
        store.setStoreId(1);

        staff = new Staff();
        staff.setStaffId(1);
        staff.setUsername("user");
        staff.setStoreId(1);
        staff.setStore(store);

        film = new Film();
        film.setFilmId(10);
        film.setTitle("ACADEMY DINOSAUR");
    }

    @Test
    @DisplayName("getInventory — should compute available copies")
    void shouldComputeInventoryForFilm() {
        Inventory inv = new Inventory();
        inv.setInventoryId(100);
        inv.setFilm(film);

        when(inventoryRepository.findById(10)).thenReturn(Optional.of(inv));
        when(authUtil.getLoggedInUsername()).thenReturn("user");
        when(staffRepository.findByUsername("user")).thenReturn(Optional.of(staff));
        when(inventoryRepository.countByFilm_FilmIdAndStore_StoreId(10, 1)).thenReturn(5L);
        when(rentalRepository.countByInventory_Film_FilmIdAndInventory_Store_StoreIdAndReturnDateIsNull(10, 1))
                .thenReturn(2L);

        InventoryDto dto = inventoryService.getInventory(10);

        assertThat(dto.getFilmId()).isEqualTo(10);
        assertThat(dto.getMovieTitle()).isEqualTo("ACADEMY DINOSAUR");
        assertThat(dto.getTotalCopies()).isEqualTo(5L);
        assertThat(dto.getRentedCopies()).isEqualTo(2L);
        assertThat(dto.getAvailableCopies()).isEqualTo(3L);
    }

    @Test
    @DisplayName("getInventory — should throw when film inventory missing")
    void shouldThrowWhenInventoryMissing() {
        when(inventoryRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> inventoryService.getInventory(99))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("film not found");
    }

    @Test
    @DisplayName("getStoreInventory — should return empty page when no films")
    void shouldReturnEmptyStoreInventory() {
        when(authUtil.getLoggedInUsername()).thenReturn("user");
        when(staffRepository.findByUsername("user")).thenReturn(Optional.of(staff));
        when(filmRepository.findDistinctByInventories_Store_StoreId(eq(1), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of()));

        Page<InventoryDto> page = inventoryService.getStoreInventory(PageRequest.of(0, 5));

        assertThat(page.getContent()).isEmpty();
    }

    @Test
    @DisplayName("getStoreInventory — should aggregate totals and rented per film")
    void shouldAggregateStoreInventory() {
        when(authUtil.getLoggedInUsername()).thenReturn("user");
        when(staffRepository.findByUsername("user")).thenReturn(Optional.of(staff));

        when(filmRepository.findDistinctByInventories_Store_StoreId(eq(1), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(film)));

        when(inventoryRepository.countByFilm_FilmIdAndStore_StoreId(10, 1)).thenReturn(2L);
        when(rentalRepository.countByInventory_Film_FilmIdAndInventory_Store_StoreIdAndReturnDateIsNull(10, 1))
                .thenReturn(1L);

        Page<InventoryDto> page = inventoryService.getStoreInventory(PageRequest.of(0, 5));

        assertThat(page.getContent()).hasSize(1);
        InventoryDto dto = page.getContent().get(0);
        assertThat(dto.getTotalCopies()).isEqualTo(2L);
        assertThat(dto.getRentedCopies()).isEqualTo(1L);
        assertThat(dto.getAvailableCopies()).isEqualTo(1L);
    }

    @Test
    @DisplayName("getStoreInventory — should search by title when search provided")
    void shouldSearchStoreInventoryByTitle() {
        when(authUtil.getLoggedInUsername()).thenReturn("user");
        when(staffRepository.findByUsername("user")).thenReturn(Optional.of(staff));

        when(filmRepository.findDistinctByInventories_Store_StoreIdAndTitleContainingIgnoreCase(
                eq(1), eq("acad"), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of()));

        Page<InventoryDto> page = inventoryService.getStoreInventory("acad", PageRequest.of(0, 5));

        assertThat(page.getContent()).isEmpty();
    }

    @Test
    @DisplayName("findFirstAvailableInventoryId — should return id when copy not rented")
    void shouldReturnFirstAvailableInventoryId() {
        when(authUtil.getLoggedInUsername()).thenReturn("user");
        when(staffRepository.findByUsername("user")).thenReturn(Optional.of(staff));

        Inventory inv1 = new Inventory();
        inv1.setInventoryId(101);
        inv1.setFilm(film);

        Inventory inv2 = new Inventory();
        inv2.setInventoryId(102);
        inv2.setFilm(film);

        when(inventoryRepository.findByStore_StoreIdAndFilm_FilmIdIn(eq(1), eq(List.of(10))))
                .thenReturn(List.of(inv1, inv2));

        Rental rental = new Rental();
        rental.setInventory(inv1);

        when(rentalRepository
                .findByInventory_Store_StoreIdAndInventory_Film_FilmIdInAndReturnDateIsNull(
                        eq(1), eq(List.of(10))))
                .thenReturn(List.of(rental));

        Integer id = inventoryService.findFirstAvailableInventoryId(10);

        assertThat(id).isEqualTo(102);
    }

    @Test
    @DisplayName("findFirstAvailableInventoryId — should return null when no copies exist")
    void shouldReturnNullWhenNoCopies() {
        when(authUtil.getLoggedInUsername()).thenReturn("user");
        when(staffRepository.findByUsername("user")).thenReturn(Optional.of(staff));

        when(inventoryRepository.findByStore_StoreIdAndFilm_FilmIdIn(eq(1), eq(List.of(10))))
                .thenReturn(List.of());

        Integer id = inventoryService.findFirstAvailableInventoryId(10);

        assertThat(id).isNull();
    }

    @Test
    @DisplayName("findFirstAvailableInventoryId — should return null when all copies rented")
    void shouldReturnNullWhenAllRented() {
        when(authUtil.getLoggedInUsername()).thenReturn("user");
        when(staffRepository.findByUsername("user")).thenReturn(Optional.of(staff));

        Inventory inv1 = new Inventory();
        inv1.setInventoryId(101);
        inv1.setFilm(film);

        when(inventoryRepository.findByStore_StoreIdAndFilm_FilmIdIn(eq(1), eq(List.of(10))))
                .thenReturn(List.of(inv1));

        Rental rental = new Rental();
        rental.setInventory(inv1);

        when(rentalRepository
                .findByInventory_Store_StoreIdAndInventory_Film_FilmIdInAndReturnDateIsNull(
                        eq(1), eq(List.of(10))))
                .thenReturn(List.of(rental));

        Integer id = inventoryService.findFirstAvailableInventoryId(10);

        assertThat(id).isNull();
    }
}
