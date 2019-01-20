package com.glovoapp.backender.service;

import com.glovoapp.backender.model.Courier;
import com.glovoapp.backender.model.Location;
import com.glovoapp.backender.model.Order;
import com.glovoapp.backender.model.Vehicle;
import com.glovoapp.backender.repository.CourierRepository;
import com.glovoapp.backender.repository.OrderRepository;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    private static final String EXCEPTION_MESSAGE = "An exception";
    private static final String COURIER_ID = "1";
    private static final Courier COURIER = Courier.builder().id(COURIER_ID).build();
    private static final Location LOCATION_FRANCES_MACIA = new Location(41.3925603, 2.1418532);
    private static final Location LOCATION_PLACA_CATALUNYA = new Location(41.3870194, 2.1678584);
    private static final String LARGE_ORDER_ID = "1";
    private static final Order LARGE_ORDER = Order.builder().id(LARGE_ORDER_ID).description("1 LargeFood").pickup(LOCATION_FRANCES_MACIA).build();
    private static final String SMALL_ORDER_ID = "2";
    private static final Order SMALL_ORDER = Order.builder().id(SMALL_ORDER_ID).description("1 Sausage").pickup(LOCATION_PLACA_CATALUNYA).build();

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CourierRepository courierRepository;

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(orderRepository,
                courierRepository,
                ImmutableList.of("LargeFood"),
                ImmutableMap.of(Vehicle.BICYCLE, 1d, Vehicle.MOTORCYCLE, 100d));
    }

    @Test
    void orders_noOrders_empty() {
        when(orderRepository.findAll()).thenReturn(ImmutableList.of());

        assertEquals(ImmutableList.of(), orderService.orders());
    }

    @Test
    void orders_someOrders_ok() {
        when(orderRepository.findAll()).thenReturn(ImmutableList.of(LARGE_ORDER, SMALL_ORDER));

        assertEquals(ImmutableList.of(LARGE_ORDER, SMALL_ORDER), orderService.orders());
    }

    @Test
    void orders_exception_bubbles() {
        when(orderRepository.findAll()).thenThrow(new RuntimeException(EXCEPTION_MESSAGE));

        assertThrows(RuntimeException.class, () -> orderService.orders(), EXCEPTION_MESSAGE);
    }

    @Test
    void ordersForCourier_noCouriers_empty() {
        when(courierRepository.findById(COURIER_ID)).thenReturn(Optional.of(COURIER));
        when(orderRepository.findAll()).thenReturn(ImmutableList.of());

        assertEquals(ImmutableList.of(), orderService.ordersForCourier(COURIER_ID));
    }

    @Test
    void ordersForCourier_boxOrderNoBoxCourier_empty() {
        when(courierRepository.findById(COURIER_ID))
                .thenReturn(Optional.of(Courier.builder()
                        .location(LOCATION_FRANCES_MACIA)
                        .box(false)
                        .vehicle(Vehicle.MOTORCYCLE)
                        .build()));
        when(orderRepository.findAll()).thenReturn(ImmutableList.of(LARGE_ORDER));

        assertEquals(ImmutableList.of(), orderService.ordersForCourier(COURIER_ID));
    }

    @Test
    void ordersForCourier_farOrderBicycle_empty() {
        when(courierRepository.findById(COURIER_ID))
                .thenReturn(Optional.of(Courier.builder()
                        .location(LOCATION_FRANCES_MACIA)
                        .box(false)
                        .vehicle(Vehicle.BICYCLE)
                        .build()));
        when(orderRepository.findAll()).thenReturn(ImmutableList.of(Order.builder().description("").pickup(LOCATION_PLACA_CATALUNYA).build()));

        assertEquals(ImmutableList.of(), orderService.ordersForCourier(COURIER_ID));
    }

    @Test
    void ordersForCourier_nearOrder_returns() {
        when(courierRepository.findById(COURIER_ID))
                .thenReturn(Optional.of(Courier.builder()
                        .location(LOCATION_FRANCES_MACIA)
                        .box(false)
                        .vehicle(Vehicle.BICYCLE)
                        .build()));
        Order order = Order.builder().description("").pickup(LOCATION_FRANCES_MACIA).build();
        when(orderRepository.findAll()).thenReturn(ImmutableList.of(order));

        assertEquals(ImmutableList.of(order), orderService.ordersForCourier(COURIER_ID));
    }

    @Test
    void ordersForCourier_twoGoodOrders_returnsBoth() {
        when(courierRepository.findById(COURIER_ID))
                .thenReturn(Optional.of(Courier.builder()
                        .location(LOCATION_FRANCES_MACIA)
                        .box(false)
                        .vehicle(Vehicle.MOTORCYCLE)
                        .build()));
        Order order1 = Order.builder().id("1").description("").pickup(LOCATION_FRANCES_MACIA).build();
        Order order2 = Order.builder().id("2").description("").pickup(LOCATION_PLACA_CATALUNYA).build();
        when(orderRepository.findAll()).thenReturn(ImmutableList.of(order1, order2));

        assertEquals(ImmutableList.of(order1, order2), orderService.ordersForCourier(COURIER_ID));
    }

    @Test
    void ordersForCourier_oneFarOrder_returnsOther() {
        when(courierRepository.findById(COURIER_ID))
                .thenReturn(Optional.of(Courier.builder()
                        .location(LOCATION_PLACA_CATALUNYA)
                        .box(false)
                        .vehicle(Vehicle.BICYCLE)
                        .build()));
        Order order1 = Order.builder().id("1").description("").pickup(LOCATION_FRANCES_MACIA).build();
        Order order2 = Order.builder().id("2").description("").pickup(LOCATION_PLACA_CATALUNYA).build();
        when(orderRepository.findAll()).thenReturn(ImmutableList.of(order1, order2));

        assertEquals(ImmutableList.of(order2), orderService.ordersForCourier(COURIER_ID));
    }

    @Test
    void ordersForCourier_exception_bubbles() {
        when(courierRepository.findById(COURIER_ID)).thenReturn(Optional.of(COURIER));
        when(orderRepository.findAll()).thenThrow(new RuntimeException(EXCEPTION_MESSAGE));

        assertThrows(RuntimeException.class, () -> orderService.ordersForCourier(COURIER_ID), EXCEPTION_MESSAGE);
    }

    @Test
    void ordersForCourier_invalidCourier_exception() {
        when(courierRepository.findById(COURIER_ID)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> orderService.ordersForCourier(COURIER_ID), EXCEPTION_MESSAGE);
    }
}