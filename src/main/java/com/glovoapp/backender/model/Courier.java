package com.glovoapp.backender.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Courier {
    String id;
    String name;
    Boolean box;
    Vehicle vehicle;
    Location location;
}
