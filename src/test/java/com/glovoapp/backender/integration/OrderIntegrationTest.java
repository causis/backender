package com.glovoapp.backender.integration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.util.NestedServletException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
class OrderIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void orders_ok() throws Exception {
        mockMvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andExpect(content().string("[{\"id\":\"order-1\",\"description\":\"I want a pizza cut into very small slices\"}]"));
    }

    @Test
    void orders_invalidCourierId_exception() {
        assertThrows(NestedServletException.class, () -> mockMvc.perform(get("/orders/courier-5")), "The Courier with id 'courier-5' was not found.");
    }

    @Test
    void orders_courierId_ok() throws Exception {
        mockMvc.perform(get("/orders/courier-1"))
                .andExpect(status().isOk())
                .andExpect(content().string("[{\"id\":\"order-1\",\"description\":\"I want a pizza cut into very small slices\"}]"));
    }

    @Test
    void orders_courierNoBox_empty() throws Exception {
        mockMvc.perform(get("/orders/courier-2"))
                .andExpect(status().isOk())
                .andExpect(content().string("[]"));
    }

    @Test
    void orders_courierFarWithBicicle_empty() throws Exception {
        mockMvc.perform(get("/orders/courier-3"))
                .andExpect(status().isOk())
                .andExpect(content().string("[]"));
    }
}
