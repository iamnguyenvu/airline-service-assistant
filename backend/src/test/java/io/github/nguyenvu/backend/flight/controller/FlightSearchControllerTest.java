package io.github.nguyenvu.backend.flight.controller;

import io.github.nguyenvu.backend.flight.dto.FlightSearchCriteria;
import io.github.nguyenvu.backend.flight.dto.FlightSearchResult;
import io.github.nguyenvu.backend.flight.entity.FlightSnapshot;
import io.github.nguyenvu.backend.flight.service.FlightSearchService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FlightSearchController.class)
@Import({io.github.nguyenvu.backend.config.GlobalExceptionHandler.class, 
         io.github.nguyenvu.backend.config.SecurityConfig.class})
class FlightSearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FlightSearchService flightSearchService;

    @Test
    void shouldSearchFlightsSuccessfully() throws Exception {
        // Given
        FlightSearchResult result = FlightSearchResult.builder()
            .flights(List.of())
            .totalElements(0)
            .totalPages(0)
            .currentPage(0)
            .pageSize(20)
            .build();
        
        when(flightSearchService.search(any(), anyInt(), anyInt()))
            .thenReturn(result);

        // When & Then
        mockMvc.perform(post("/api/flights/search")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "depIata": "SGN",
                  "arrIata": "HAN",
                  "snapshotDate": "2025-12-01"
                }
                """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElements").value(0))
            .andExpect(jsonPath("$.currentPage").value(0));
    }

    @Test
    void shouldGetCheapestFlightsSuccessfully() throws Exception {
        // Given
        FlightSnapshot flight = new FlightSnapshot();
        flight.setFlightNo("VN101");
        flight.setPriceCents(1_000_000L);
        
        when(flightSearchService.findCheapestFlights(
            anyString(), anyString(), any(LocalDate.class), anyInt()))
            .thenReturn(List.of(flight));

        // When & Then
        mockMvc.perform(get("/api/flights/cheapest")
            .param("dep", "SGN")
            .param("arr", "HAN")
            .param("date", "2025-12-01")
            .param("limit", "5"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].flightNo").value("VN101"))
            .andExpect(jsonPath("$[0].priceCents").value(1_000_000));
    }

    @Test
    void shouldReturnBadRequestForInvalidIataCode() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/flights/cheapest")
            .param("dep", "SG")  // Invalid: only 2 chars
            .param("arr", "HAN")
            .param("date", "2025-12-01"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Invalid request"))
            .andExpect(jsonPath("$.message").value("Invalid IATA code format"));
    }

    @Test
    void shouldGetAvailableCarriers() throws Exception {
        // Given
        when(flightSearchService.getAvailableCarriers(anyString(), anyString()))
            .thenReturn(List.of("VN", "VJ"));

        // When & Then
        mockMvc.perform(get("/api/flights/carriers")
            .param("dep", "SGN")
            .param("arr", "HAN"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0]").value("VN"))
            .andExpect(jsonPath("$[1]").value("VJ"));
    }

    @Test
    void shouldCheckAvailability() throws Exception {
        // Given
        when(flightSearchService.hasFlights(anyString(), anyString(), any(LocalDate.class)))
            .thenReturn(true);

        // When & Then
        mockMvc.perform(get("/api/flights/availability")
            .param("dep", "SGN")
            .param("arr", "HAN")
            .param("date", "2025-12-01"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.available").value(true))
            .andExpect(jsonPath("$.route").value("SGN-HAN"));
    }
}
