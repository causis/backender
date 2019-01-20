package com.glovoapp.backender.service;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String type, String id) {
        super(String.format("The %s with id '%s' was not found.", type, id));
    }
}
