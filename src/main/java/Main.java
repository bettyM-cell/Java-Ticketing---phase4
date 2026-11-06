import com.ticket.domain.*;
import com.ticket.exception.InvalidSeatException;
import com.ticket.service.LruCache;
import com.ticket.service.TicketingService;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

public class Main {
    private static final TicketingService ticketingService = new TicketingService();
    private static final LruCache<String, Event> eventCache = new LruCache<>(5);

    private static final List<Venue> venues = new ArrayList<>();
    private static final List<Seat> seats = new ArrayList<>();
    private static final String EVENT_ID = "E-01";

    public static void main(String[] args) {
        bootstrapInitialData();

        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        System.out.println("=================================================");
        System.out.println("  CONCURRENT JAVA TICKETING SYSTEM (PHASE 2)     ");
        System.out.println("=================================================");

        while (running) {
            System.out.println("\n--- MAIN MENU ---");
            System.out.println("1. Browse Events (Uses Hot Cache Lookup)");
            System.out.println("2. View Seating Arrangements Status");
            System.out.println("3. Place a Short 10-Second Hold (Watch Sweeper)");
            System.out.println("4. View All System Active Transaction Logs");
            System.out.println("5. Exit Application");
            System.out.print("Select an option (1-5): ");

            String choice = scanner.nextLine();
            try {
                switch (choice) {
                    case "1" -> handleCachedBrowse();
                    case "2" -> handleViewSeats();
                    case "3" -> handlePlaceHold(scanner);
                    case "4" -> handleViewLogs();
                    case "5" -> {
                        System.out.println("Shutting down background thread pools... Goodbye!");
                        ticketingService.shutdown();
                        running = false;
                    }
                    default -> System.out.println("⚠️ Invalid input. Choose options 1 through 5.");
                }
            } catch (InvalidSeatException ex) {
                System.out.println("❌ CONCURRENCY VIOLATION: " + ex.getMessage());
            } catch (Exception ex) {
                System.out.println("❌ UNEXPECTED ERROR: " + ex.getMessage());
            }
        }
        scanner.close();
    }

    private static void bootstrapInitialData() {
        Venue venue = new Venue("V-01", "Metropolitan Grand Opera", "742 Evergreen Terrace", ZoneId.of("America/New_York"));
        venues.add(venue);

        ZonedDateTime eventStart = ZonedDateTime.of(2026, 12, 15, 19, 30, 0, 0, venue.timezone());
        Event event = new Event(EVENT_ID, venue.id(), "The Phantom of the Opera Grand Premiere", eventStart, eventStart.plusHours(3), EventStatus.ACTIVE);

        // Populate our LRU cache for high performance
        eventCache.put(EVENT_ID, event);

        seats.add(new Seat("S-01", venue.id(), "Orchestra", "Row-A", 1, Map.of()));
        seats.add(new Seat("S-02", venue.id(), "Orchestra", "Row-A", 2, Map.of()));
        seats.add(new Seat("S-03", venue.id(), "Balcony", "Row-B", 1, Map.of()));
    }

    private static void handleCachedBrowse() {
        System.out.println("\n--- BROWSE EVENTS [LRU CACHE HIT] ---");
        Optional<Event> cachedEvent = eventCache.get(EVENT_ID);
        if (cachedEvent.isPresent()) {
            Event e = cachedEvent.get();
            System.out.printf("[CACHE LOOKUP SUCCESS] %s - Live Status: %s\n", e.title(), e.status());
        }
    }

    private static void handleViewSeats() {
        System.out.println("\n--- CURRENT SEATING DEPLOYMENTS MATRIX ---");
        Map<String, Reservation> activeReservations = ticketingService.getReservationTable();

        for (Seat seat : seats) {
            // Check if there is an active holding blocking this seat
            boolean isHeld = activeReservations.values().stream()
                    .anyMatch(r -> r.status() == ReservationStatus.HOLD && !r.isExpired());

            String availability = isHeld ? "[SECURED HOLD]" : "[VACANT AVAILABLE]";
            System.out.printf("  Seat ID: %s | Section: %-10s | %s\n", seat.id(), seat.section(), availability);
        }
    }

    private static void handlePlaceHold(Scanner scanner) {
        System.out.println("\n--- PLACE CONCURRENT SHORT-TERM HOLD ---");
        System.out.print("Enter target Seat ID to secure (e.g., S-01): ");
        String seatId = scanner.nextLine();

        Seat targetSeat = seats.stream().filter(s -> s.id().equalsIgnoreCase(seatId)).findFirst()
                .orElseThrow(() -> new InvalidSeatException("Seat reference does not exist."));

        System.out.print("Enter Customer Contact Email address: ");
        String email = scanner.nextLine();

        // Place a short 10-second hold to let you see the scheduler thread jump in
        long holdTimeSeconds = 10;
        Reservation res = ticketingService.placeHold(EVENT_ID, targetSeat, email, holdTimeSeconds);

        System.out.printf("🎉 Success! Hold %s secured for 10 seconds. Quickly press option 2 or 4 to see it active!\n", res.id());
    }

    private static void handleViewLogs() {
        System.out.println("\n--- LIVE TRANSACTION DATA REGISTRY LOGS ---");
        Map<String, Reservation> activeReservations = ticketingService.getReservationTable();
        if (activeReservations.isEmpty()) {
            System.out.println("No transactional holdings found in thread-safe storage mappings.");
            return;
        }
        activeReservations.forEach((id, res) ->
                System.out.printf("  Transaction Ref: %s | Email: %s | Status Flag: %s (Expires: %s)\n",
                        id, res.customerEmail(), res.status(), res.holdExpiresAt()));
    }
}
