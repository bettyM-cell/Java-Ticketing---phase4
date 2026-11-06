package com.ticket.controller;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Counter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/monitoring")
public class MetricsController {

    private final MeterRegistry meterRegistry;

    public MetricsController(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    /**
     * Exposes a real-time operational status map of the ticketing engine.
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getEngineTelemetry() {
        Map<String, Object> stats = new HashMap<>();

        // Find our custom hold counter metric
        Counter holdCounter = meterRegistry.find("ticket.holds.created").counter();
        double totalHoldsProcessed = (holdCounter != null) ? holdCounter.count() : 0.0;

        stats.put("engine.status", "HEALTHY");
        stats.put("total.holds.processed", totalHoldsProcessed);
        stats.put("jvm.threads.active", Thread.activeCount());
        stats.put("system.timestamp", System.currentTimeMillis());

        return ResponseEntity.ok(stats);
    }
}
