package com.glovoapp.backender.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Order {
    String id;
    String description;
    Boolean food;
    Boolean vip;
    Location pickup;
    Location delivery;
}
