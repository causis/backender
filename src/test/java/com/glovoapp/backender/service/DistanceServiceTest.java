package com.glovoapp.backender.service;

import com.glovoapp.backender.model.Location;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DistanceServiceTest {
    @Test
    void smokeTest() {
        Location francescMacia = new Location(41.3925603, 2.1418532);
        Location placaCatalunya = new Location(41.3870194, 2.1678584);

        // More or less 2km from Francesc Macia to Placa Catalunya
        assertEquals(2.0, DistanceService.calculateDistance(francescMacia, placaCatalunya), 0.5);
    }

}