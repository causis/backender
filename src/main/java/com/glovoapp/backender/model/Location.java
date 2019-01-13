package com.glovoapp.backender.model;

import lombok.Builder;
import lombok.Value;

import java.util.Objects;

@Value
public class Location {
    Double lat;
    Double lon;
}
