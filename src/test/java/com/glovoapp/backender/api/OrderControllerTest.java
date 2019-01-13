package com.glovoapp.backender.api;

import com.glovoapp.backender.api.model.OrderVM;
import com.glovoapp.backender.model.Order;
import com.glovoapp.backender.repository.OrderRepository;
import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {
    private static final String EXCEPTION_MSG = "Whoops... an exception";

    private static final String ORDER_1_DESCRIPTION = "Order 1";
    private static final String ORDER_1_ID = "1";
    private static final String ORDER_2_DESCRIPTION = "Order 2";
    private static final String ORDER_2_ID = "2";

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderController orderController;

    @Test
    void orders_noOrders_empty() {
        Mockito.when(orderRepository.findAll())
                .thenReturn(ImmutableList.of());

        assertEquals(ImmutableList.of(), orderController.orders());
    }

    @Test
    void orders_someOrders_ok() {
        Mockito.when(orderRepository.findAll())
                .thenReturn(ImmutableList.of(Order.builder().id(ORDER_1_ID).description(ORDER_1_DESCRIPTION).build(),
                        Order.builder().id(ORDER_2_ID).description(ORDER_2_DESCRIPTION).build()));

        assertEquals(
                ImmutableList.of(new OrderVM(ORDER_1_ID, ORDER_1_DESCRIPTION), new OrderVM(ORDER_2_ID, ORDER_2_DESCRIPTION)),
                orderController.orders());
    }

    @Test
    void orders_orderWithNullData_ok() {
        Mockito.when(orderRepository.findAll())
                .thenReturn(ImmutableList.of(Order.builder().build()));

        assertEquals(
                ImmutableList.of(new OrderVM(null, null)),
                orderController.orders());
    }

    @Test
    void orders_exception_bubbles() {
        Mockito.when(orderRepository.findAll())
                .thenThrow(new RuntimeException(EXCEPTION_MSG));

        assertThrows(RuntimeException.class, () -> orderController.orders(), EXCEPTION_MSG);
    }
}