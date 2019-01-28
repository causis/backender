package com.glovoapp.backender.service;

import com.glovoapp.backender.model.Courier;
import com.glovoapp.backender.model.Location;
import com.glovoapp.backender.model.Order;
import com.glovoapp.backender.model.Vehicle;
import com.glovoapp.backender.repository.CourierRepository;
import com.glovoapp.backender.repository.OrderRepository;
import com.glovoapp.backender.service.OrderService.SlotPriority;
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
    private static final Location LOCATION_ORIGIN = new Location(1d, 1d);
    private static final Location LOCATION_3KM = new Location(2d, 2d);
    private static final Location LOCATION_10KM = new Location(3d, 3d);
    private static final Location LOCATION_10_1KM = new Location(3d, 3d);
    private static final Location LOCATION_10_2KM = new Location(3d, 4d);
    private static final Location LOCATION_10_3KM = new Location(3d, 5d);
    private static final Location LOCATION_15KM = new Location(4d, 4d);
    private static final String LARGE_ORDER_ID = "1";
    private static final Order LARGE_ORDER = Order.builder().id(LARGE_ORDER_ID).description("1 LargeFood").pickup(LOCATION_3KM).build();
    private static final String SMALL_ORDER_ID = "2";
    private static final Order SMALL_ORDER = Order.builder().id(SMALL_ORDER_ID).description("1 Sausage").pickup(LOCATION_10KM).build();

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CourierRepository courierRepository;

    @Mock(lenient = true)
    private DistanceCalculator distanceCalculator;

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(orderRepository,
                courierRepository,
                distanceCalculator,
                ImmutableList.of("LargeFood"),
                ImmutableMap.of(Vehicle.BICYCLE, 5d, Vehicle.MOTORCYCLE, 100d),
                0.5,
                ImmutableList.of(SlotPriority.VIP, SlotPriority.FOOD, SlotPriority.ALL));

        when(distanceCalculator.calculateDistance(LOCATION_ORIGIN, LOCATION_ORIGIN)).thenReturn(0d);
        when(distanceCalculator.calculateDistance(LOCATION_ORIGIN, LOCATION_3KM)).thenReturn(3d);
        when(distanceCalculator.calculateDistance(LOCATION_ORIGIN, LOCATION_10KM)).thenReturn(10d);
        when(distanceCalculator.calculateDistance(LOCATION_ORIGIN, LOCATION_10_1KM)).thenReturn(10.1d);
        when(distanceCalculator.calculateDistance(LOCATION_ORIGIN, LOCATION_10_2KM)).thenReturn(10.2d);
        when(distanceCalculator.calculateDistance(LOCATION_ORIGIN, LOCATION_10_3KM)).thenReturn(10.3d);
        when(distanceCalculator.calculateDistance(LOCATION_ORIGIN, LOCATION_15KM)).thenReturn(15d);
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
                .thenReturn(Optional.of(courierFor(Vehicle.MOTORCYCLE, false)));
        when(orderRepository.findAll()).thenReturn(ImmutableList.of(LARGE_ORDER));

        assertEquals(ImmutableList.of(), orderService.ordersForCourier(COURIER_ID));
    }

    @Test
    void ordersForCourier_boxOrderBoxCourier_returns() {
        when(courierRepository.findById(COURIER_ID))
                .thenReturn(Optional.of(courierFor(Vehicle.MOTORCYCLE, true)));
        when(orderRepository.findAll()).thenReturn(ImmutableList.of(LARGE_ORDER));

        assertEquals(ImmutableList.of(LARGE_ORDER), orderService.ordersForCourier(COURIER_ID));
    }

    @Test
    void ordersForCourier_farOrderBicycle_empty() {
        when(courierRepository.findById(COURIER_ID))
                .thenReturn(Optional.of(courierFor(Vehicle.BICYCLE, false)));
        when(orderRepository.findAll()).thenReturn(ImmutableList.of(Order.builder().description("").pickup(LOCATION_10KM).build()));

        assertEquals(ImmutableList.of(), orderService.ordersForCourier(COURIER_ID));
    }

    @Test
    void ordersForCourier_nearOrder_returns() {
        when(courierRepository.findById(COURIER_ID))
                .thenReturn(Optional.of(courierFor(Vehicle.BICYCLE, false)));
        Order order = Order.builder().description("").pickup(LOCATION_3KM).build();
        when(orderRepository.findAll()).thenReturn(ImmutableList.of(order));

        assertEquals(ImmutableList.of(order), orderService.ordersForCourier(COURIER_ID));
    }

    @Test
    void ordersForCourier_twoGoodOrders_returnsBoth() {
        when(courierRepository.findById(COURIER_ID))
                .thenReturn(Optional.of(courierFor(Vehicle.MOTORCYCLE, false)));
        Order order1 = Order.builder().id("1").description("").pickup(LOCATION_3KM).build();
        Order order2 = Order.builder().id("2").description("").pickup(LOCATION_10KM).build();
        when(orderRepository.findAll()).thenReturn(ImmutableList.of(order1, order2));

        assertEquals(ImmutableList.of(order1, order2), orderService.ordersForCourier(COURIER_ID));
    }

    @Test
    void ordersForCourier_oneFarOrder_returnsOther() {
        when(courierRepository.findById(COURIER_ID))
                .thenReturn(Optional.of(courierFor(Vehicle.BICYCLE, false)));
        Order order1 = Order.builder().id("1").description("").pickup(LOCATION_10KM).build();
        Order order2 = Order.builder().id("2").description("").pickup(LOCATION_3KM).build();
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

    @Test
    void ordersForCourier_twoSlotsAllPossibilities_sortsCorrectly() {
        when(courierRepository.findById(COURIER_ID))
                .thenReturn(Optional.of(courierFor(Vehicle.MOTORCYCLE, false)));

        Order order1_1 = Order.builder().id("1").description("").vip(true).food(false).pickup(LOCATION_3KM).build();
        Order order1_2 = Order.builder().id("2").description("").vip(false).food(true).pickup(LOCATION_3KM).build();
        Order order1_3 = Order.builder().id("3").description("").vip(false).food(false).pickup(LOCATION_3KM).build();
        Order order2_1 = Order.builder().id("4").description("").vip(true).food(false).pickup(LOCATION_10KM).build();
        Order order2_2 = Order.builder().id("5").description("").vip(false).food(true).pickup(LOCATION_10KM).build();
        Order order2_3 = Order.builder().id("6").description("").vip(false).food(false).pickup(LOCATION_10KM).build();
        when(orderRepository.findAll()).thenReturn(ImmutableList.of(order1_3, order1_2, order1_1, order2_1, order2_2, order2_3));

        assertEquals(ImmutableList.of(order1_1, order1_2, order1_3, order2_1, order2_2, order2_3),
                orderService.ordersForCourier(COURIER_ID));
    }

    @Test
    void ordersForCourier_oneSlotSomePossibilities_sortsCorrectly() {
        when(courierRepository.findById(COURIER_ID))
                .thenReturn(Optional.of(courierFor(Vehicle.MOTORCYCLE, false)));

        Order order2_1 = Order.builder().id("1").description("").vip(true).food(false).pickup(LOCATION_10KM).build();
        Order order2_2 = Order.builder().id("2").description("").vip(true).food(true).pickup(LOCATION_10_1KM).build();
        Order order2_3 = Order.builder().id("3").description("").vip(true).food(false).pickup(LOCATION_10_2KM).build();
        Order order2_4 = Order.builder().id("4").description("").vip(true).food(false).pickup(LOCATION_10_3KM).build();
        Order order2_5 = Order.builder().id("5").description("").vip(false).food(true).pickup(LOCATION_10_1KM).build();
        Order order2_6 = Order.builder().id("6").description("").vip(false).food(true).pickup(LOCATION_10_2KM).build();
        when(orderRepository.findAll()).thenReturn(ImmutableList.of(order2_6, order2_5, order2_4, order2_1, order2_2, order2_3));

        assertEquals(ImmutableList.of(order2_1, order2_2, order2_3, order2_4, order2_5, order2_6),
                orderService.ordersForCourier(COURIER_ID));
    }

    private Courier courierFor(Vehicle vehicle, boolean box) {
        return Courier.builder().location(LOCATION_ORIGIN).box(box).vehicle(vehicle).build();
    }
}