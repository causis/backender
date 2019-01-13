package com.glovoapp.backender.api.model;

import lombok.Value;

/**
 * To be used for exposing order information through the API
 */
@Value
public class OrderVM {
    String id;
    String description;
}
