package com.glovoapp.backender.service;

import com.glovoapp.backender.model.Courier;
import com.glovoapp.backender.model.Order;
import com.glovoapp.backender.model.Vehicle;
import com.glovoapp.backender.repository.CourierRepository;
import com.glovoapp.backender.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrderService {
    private OrderRepository orderRepository;
    private CourierRepository courierRepository;
    private List<String> boxableFoods;
    private Map<Vehicle, Double> vehiclesRange;

    public OrderService(OrderRepository orderRepository,
                        CourierRepository courierRepository,
                        @Value("#{'${backender.boxable_foods}'.split(',')}") List<String> boxableFoods,
                        @Value("#{${backender.vehicles_range}}") Map<Vehicle, Double> vehiclesRange) {
        this.orderRepository = orderRepository;
        this.courierRepository = courierRepository;
        //Assuming upper/lower case doesn't matter when comparing food names
        this.boxableFoods = boxableFoods.stream().map(String::toLowerCase).collect(Collectors.toList());
        this.vehiclesRange = vehiclesRange;
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
                .filter(order -> canSeeOrder(courier, order))
                .collect(Collectors.toList());
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
    private boolean canSeeOrder(Courier courier, Order order) {
        return (!needsBox(order) || courier.getBox()) &&
                //If the vehicle is not configured, the courier won't be assigned but the service won't fail and
                //another courier will deliver the order.
                //This should log an error so somebody knows there's a problem in the config.
                (vehiclesRange.getOrDefault(courier.getVehicle(), -1d) >=
                        DistanceCalculator.calculateDistance(courier.getLocation(), order.getPickup()));
    }

    private boolean needsBox(Order order) {
        return boxableFoods.stream()
                .anyMatch(boxableFood -> order.getDescription().toLowerCase().contains(boxableFood));
    }
}
