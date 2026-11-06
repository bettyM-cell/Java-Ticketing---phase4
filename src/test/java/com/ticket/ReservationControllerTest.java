package com.ticket;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test") // Binds to our isolated application-test.properties profile configuration
public class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testCreateSeatHoldEndpointSuccessfully() throws Exception {
        mockMvc.perform(post("/api/reservations/hold")
                        .param("eventId", "E-01")
                        .param("seatId", "S-05")
                        .param("email", "manager@company.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.customerEmail").value("manager@company.com"))
                .andExpect(jsonPath("$.status").value("HOLD"));
    }
}
