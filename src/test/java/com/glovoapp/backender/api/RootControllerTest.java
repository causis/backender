package com.glovoapp.backender.api;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RootControllerTest {
    private static final String MESSAGE = "Message";

    @Test
    void root_returnsMessage() {
        assertEquals(MESSAGE, new RootController(MESSAGE).root());
    }
}