package com.glovoapp.backender.repository;

import com.glovoapp.backender.model.Courier;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class CourierRepository {
    private static final String COURIERS_FILE = "/couriers.json";
    private static final List<Courier> couriers;

    static {
        try (Reader reader = new InputStreamReader(CourierRepository.class.getResourceAsStream(COURIERS_FILE))) {
            Type type = new TypeToken<List<Courier>>() {
            }.getType();
            couriers = new Gson().fromJson(reader, type);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<Courier> findById(String courierId) {
        return couriers.stream()
                .filter(courier -> courierId.equals(courier.getId()))
                .findFirst();
    }

    public List<Courier> findAll() {
        return new ArrayList<>(couriers);
    }
}
