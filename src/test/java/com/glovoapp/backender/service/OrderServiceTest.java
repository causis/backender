package com.glovoapp.backender.service;

import com.glovoapp.backender.repository.OrderRepository;
import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    private static final String EXCEPTION_MESSAGE = "An exception";
    private static final String COURIER_ID = "1";

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    void orders_ok_ok() {
        when(orderRepository.findAll()).thenReturn(ImmutableList.of());

        assertEquals(ImmutableList.of(), orderService.orders());
    }

    @Test
    void orders_exception_bubbles() {
        when(orderRepository.findAll()).thenThrow(new RuntimeException(EXCEPTION_MESSAGE));

        assertThrows(RuntimeException.class, () -> orderService.orders(), EXCEPTION_MESSAGE);
    }

    @Test
    void ordersForCourier_ok_ok() {
        when(orderRepository.findAll()).thenReturn(ImmutableList.of());

        assertEquals(ImmutableList.of(), orderService.ordersForCourier(COURIER_ID));
    }

    @Test
    void ordersForCourier_exception_bubbles() {
        when(orderRepository.findAll()).thenThrow(new RuntimeException(EXCEPTION_MESSAGE));

        assertThrows(RuntimeException.class, () -> orderService.ordersForCourier(COURIER_ID), EXCEPTION_MESSAGE);
    }
}