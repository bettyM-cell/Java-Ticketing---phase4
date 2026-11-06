package com.ticket.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ticket.domain.Reservation;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class StorageService {
    private final ObjectMapper objectMapper;

    public StorageService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    public void saveReservations(List<Reservation> reservations, Path filePath) throws IOException {
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(filePath.toFile(), reservations);
    }

    public List<Reservation> loadReservations(Path filePath) throws IOException {
        File file = filePath.toFile();
        if (!file.exists()) {
            return List.of();
        }
        return objectMapper.readValue(file, objectMapper.getTypeFactory().constructCollectionType(List.class, Reservation.class));
    }
}
