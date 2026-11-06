package com.ticket;

import com.ticket.domain.Money;
import com.ticket.domain.Seat;
import com.ticket.domain.Reservation;
import com.ticket.domain.ReservationStatus;
import com.ticket.domain.DiscountType;
import com.ticket.exception.InvalidSeatException;
import com.ticket.service.StorageService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class TicketingSystemTest {

    @Test
    void testMoneyValueEqualityAndDiscounts() {
        Money price1 = Money.of(100.00);
        Money price2 = Money.of(100.00);
        Money price3 = Money.of(50.00);

        assertEquals(price1, price2);
        assertNotEquals(price1, price3);

        Money discounted = price1.applyDiscount(DiscountType.PERCENT_10);
        assertEquals(new Money(BigDecimal.valueOf(90.00)), discounted);
    }

    @Test
    void testSeatComparatorSortingOrder() {
        Seat seat1 = new Seat("1", "V1", "B", "Row-1", 5, Map.of());
        Seat seat2 = new Seat("2", "V1", "A", "Row-2", 1, Map.of());
        Seat seat3 = new Seat("3", "V1", "A", "Row-1", 10, Map.of());

        List<Seat> seats = new ArrayList<>(List.of(seat1, seat2, seat3));
        Collections.sort(seats);

        assertEquals(seat3, seats.get(0));
        assertEquals(seat2, seats.get(1));
        assertEquals(seat1, seats.get(2));
    }

    @Test
    void testCustomExceptionThrowsPerfectly() {
        assertThrows(InvalidSeatException.class, () -> {
            throw new InvalidSeatException("Seat reference does not exist.");
        });
    }

    @Test
    void testJsonSaveAndLoadRoundTripEquals(@TempDir Path tempDir) throws IOException {
        StorageService storageService = new StorageService();
        Path testFile = tempDir.resolve("test-reservations.json");

        Reservation originalReservation = new Reservation(
                "R-789", "E-101", "operator@venue.com",
                ReservationStatus.HOLD, Instant.now(), null, Instant.now().plusSeconds(300)
        );
        List<Reservation> originalList = List.of(originalReservation);

        storageService.saveReservations(originalList, testFile);
        List<Reservation> loadedList = storageService.loadReservations(testFile);

        assertEquals(1, loadedList.size());
        assertEquals(originalReservation.id(), loadedList.get(0).id());
    }
}
