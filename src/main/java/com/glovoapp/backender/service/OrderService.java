package com.glovoapp.backender.service;

import com.glovoapp.backender.model.Courier;
import com.glovoapp.backender.model.Order;
import com.glovoapp.backender.model.Vehicle;
import com.glovoapp.backender.repository.CourierRepository;
import com.glovoapp.backender.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class OrderService {
    private OrderRepository orderRepository;
    private CourierRepository courierRepository;
    private DistanceCalculator distanceCalculator;
    private List<String> boxableFoods;
    private Map<Vehicle, Double> vehiclesRange;
    private double distanceSlotRange;
    private List<SlotPriority> slotPriority;

    public OrderService(OrderRepository orderRepository,
                        CourierRepository courierRepository,
                        DistanceCalculator distanceCalculator,
                        @Value("#{'${backender.boxable_foods}'.split(',')}") List<String> boxableFoods,
                        @Value("#{${backender.vehicles_range}}") Map<Vehicle, Double> vehiclesRange,
                        @Value("${backender.distance_slot_range}") double distanceSlotRange,
                        @Value("#{'${backender.slot_priority}'.split(',')}") List<SlotPriority> slotPriority) {

        this.orderRepository = orderRepository;
        this.courierRepository = courierRepository;
        this.distanceCalculator = distanceCalculator;
        //Assuming upper/lower case doesn't matter when comparing food names
        this.boxableFoods = boxableFoods.stream().map(String::toLowerCase).collect(Collectors.toList());
        this.vehiclesRange = vehiclesRange;
        this.distanceSlotRange = distanceSlotRange;

        this.slotPriority = slotPriority;
    }

    public List<Order> orders() {
        return orderRepository.findAll();
    }

    public List<Order> ordersForCourier(String courierId) {
        Courier courier = courierRepository.findById(courierId)
                .orElseThrow(() -> new ResourceNotFoundException("Courier", courierId));
        //Temporary implementation
        return orderRepository.findAll()
                .stream()
                .map(order -> new Candidate(courier, order))
                .filter(Candidate::canCourierSeeOrder)
                .sorted(Comparator.comparing(Candidate::distanceSlot)
                        .thenComparing(Candidate::priority)
                        .thenComparing(Candidate::getDistance))
                .map(Candidate::getOrder)
                .collect(Collectors.toList());
    }

    public enum SlotPriority implements Predicate<Order> {
        VIP {
            @Override
            public boolean test(Order order) {
                return order.getVip();
            }
        },
        FOOD {
            @Override
            public boolean test(Order order) {
                return order.getFood();
            }
        }, ALL {
            @Override
            public boolean test(Order order) {
                return true;
            }
        }
    }

    @lombok.Value
    private class Candidate {
        private Courier courier;
        private Order order;
        private double distance;

        Candidate(Courier courier, Order order) {
            this.courier = courier;
            this.order = order;
            this.distance = distanceCalculator.calculateDistance(courier.getLocation(), order.getPickup());
        }

        /**
         * Not every courier can see every order.
         * <p>
         * - If the description of the order contains the words pizza, cake or flamingo,
         * we can only show the order to the courier if they are equipped with a Glovo box.
         * <p>
         * - If the order is further than 5km to the courier, we will only show it to
         * couriers that move in motorcycle or electric scooter.
         * <p>
         * Candidate note: This method could be implemented inside the courier class for a more "OO" approach.
         * Using java dependency-injection environment going too OO can put you in a corner in the future.
         * For example, if the "DistanceCalculator" was a bean, injecting it into the Courier object would be difficult,
         * specially if the Courier class was managed by a persistence engine like any JPA implementation.
         */
        private boolean canCourierSeeOrder() {
            return boxCompatible() && isInRange();
        }

        private boolean boxCompatible() {
            return !needsBox(order) || courier.getBox();
        }

        private boolean isInRange() {
            //If the vehicle is not configured, the courier won't be assigned but the service won't fail and
            //another courier will deliver the order.
            //This should log an error so somebody knows there's a problem in the config.
            return vehiclesRange.getOrDefault(courier.getVehicle(), -1d) >= distance;
        }

        private boolean needsBox(Order order) {
            return boxableFoods.stream()
                    .anyMatch(boxableFood -> order.getDescription().toLowerCase().contains(boxableFood));
        }

        int distanceSlot() {
            return (int) (distance / distanceSlotRange); // Assuming slot is [closed (inclusive), open (exclusive))
        }

        int priority() {
            return IntStream.range(0, slotPriority.size())
                    .filter(i -> slotPriority.get(i).test(getOrder()))
                    .findFirst()
                    .orElse(Integer.MAX_VALUE);
        }
    }
}
