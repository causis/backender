package com.glovoapp.backender.repository;

import com.glovoapp.backender.model.Courier;
import com.glovoapp.backender.model.Location;
import com.glovoapp.backender.model.Vehicle;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class CourierRepositoryTest {
    @Test
    void findOneExisting() {
        Optional<Courier> courier = new CourierRepository().findById("courier-1");
        Optional<Courier> expected = Optional.of(Courier.builder()
                .id("courier-1")
                .box(true)
                .name("Manolo Escobar")
                .vehicle(Vehicle.MOTORCYCLE)
                .location(new Location(41.3965463, 2.1963997))
                .build());

        assertEquals(expected, courier);
    }

    @Test
    void findOneNotExisting() {
        assertFalse(new CourierRepository().findById("bad-courier-id").isPresent());
    }

    @Test
    void findAll() {
        List<Courier> all = new CourierRepository().findAll();
        assertFalse(all.isEmpty());
    }
}