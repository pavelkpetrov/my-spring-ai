package com.mcp.sample.server.service;

import com.mcp.sample.server.service.WeatherService.WeatherResponse;
import com.mcp.sample.server.service.WeatherService.WeatherResponse.Current;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for WeatherService.
 *
 * This test class validates the getTemperature tool functionality.
 * Tests cover successful API calls and various failure scenarios.
 *
 * Testing Strategy:
 * - Mock RestClient to isolate service logic from external API
 * - Use ReflectionTestUtils to inject mock RestClient
 * - Test success and failure scenarios
 * - Verify proper error handling and response formatting
 */
@ExtendWith(MockitoExtension.class)
class WeatherServiceTest {

    @Mock
    private RestClient restClient;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    @Mock
    private ToolContext toolContext;

    private WeatherService weatherService;

    @BeforeEach
    void setUp() {
        weatherService = new WeatherService();
        // Inject the mocked RestClient using reflection since the service creates its own
        ReflectionTestUtils.setField(weatherService, "restClient", restClient);
    }

    // =========================================================================
    // SUCCESSFUL TEST
    // =========================================================================

    @Test
    @DisplayName("getTemperature - Success: Valid coordinates return weather data")
    @SuppressWarnings("unchecked")
    void whenGetTemperature_withValidCoordinates_shouldReturnWeatherData() {
        // --- Arrange ---
        double latitude = 52.52;
        double longitude = 13.41;
        LocalDateTime currentTime = LocalDateTime.of(2025, 1, 1, 12, 0);
        double expectedTemperature = 15.5;

        // Create mock weather response
        Current mockCurrent = new Current(currentTime, 900, expectedTemperature);
        WeatherResponse mockWeatherResponse = new WeatherResponse(mockCurrent);

        // Mock the RestClient fluent API chain
        RestClient.RequestHeadersUriSpec mockSpec = mock(RestClient.RequestHeadersUriSpec.class);
        when(restClient.get()).thenReturn(mockSpec);
        when(mockSpec.uri(anyString(), any(Object.class), any(Object.class))).thenReturn(mockSpec);
        when(mockSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(WeatherResponse.class)).thenReturn(mockWeatherResponse);

        // --- Act ---
        String result = weatherService.getTemperature(latitude, longitude, toolContext);

        // --- Assert ---
        assertNotNull(result, "Result should not be null");
        assertFalse(result.isEmpty(), "Result should not be empty");

        // Verify the result contains expected data
        assertTrue(result.contains("15.5"), "Result should contain temperature value");
        assertTrue(result.contains("2025") || result.contains("temperature"), "Result should contain date or temperature reference");
        assertTrue(result.contains("temperature_2m"), "Result should contain temperature field name");

        // Verify the JSON structure
        assertTrue(result.contains("\"current\""), "Result should contain 'current' field");
        assertTrue(result.contains("\"time\""), "Result should contain 'time' field");
        assertTrue(result.contains("\"interval\""), "Result should contain 'interval' field");

        // --- Verify ---
        verify(restClient, times(1)).get();
        verify(mockSpec, times(1)).uri(anyString(), eq(latitude), eq(longitude));
        verify(mockSpec, times(1)).retrieve();
        verify(responseSpec, times(1)).body(WeatherResponse.class);
    }

    // =========================================================================
    // FAILURE TEST 1: Network Error / RestClientException
    // =========================================================================

    @Test
    @DisplayName("getTemperature - Failure: RestClient throws exception due to network error")
    @SuppressWarnings("unchecked")
    void whenGetTemperature_andRestClientThrowsException_shouldThrowRestClientException() {
        // --- Arrange ---
        double latitude = 52.52;
        double longitude = 13.41;

        // Mock the RestClient to throw RestClientException (simulating network error)
        RestClient.RequestHeadersUriSpec mockSpec = mock(RestClient.RequestHeadersUriSpec.class);
        when(restClient.get()).thenReturn(mockSpec);
        when(mockSpec.uri(anyString(), any(Object.class), any(Object.class))).thenReturn(mockSpec);
        when(mockSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(WeatherResponse.class))
                .thenThrow(new RestClientException("Connection timeout to Open-Meteo API"));

        // --- Act & Assert ---
        RestClientException exception = assertThrows(
                RestClientException.class,
                () -> weatherService.getTemperature(latitude, longitude, toolContext),
                "Should throw RestClientException when API call fails"
        );

        // Verify exception message
        assertNotNull(exception.getMessage(), "Exception message should not be null");
        assertTrue(exception.getMessage().contains("Connection timeout"),
                "Exception message should contain 'Connection timeout'");

        // --- Verify ---
        verify(restClient, times(1)).get();
        verify(mockSpec, times(1)).uri(anyString(), eq(latitude), eq(longitude));
        verify(mockSpec, times(1)).retrieve();
        verify(responseSpec, times(1)).body(WeatherResponse.class);
    }

    // =========================================================================
    // FAILURE TEST 2: API Returns Null Response
    // =========================================================================

    @Test
    @DisplayName("getTemperature - Failure: API returns null response (returns 'null' string)")
    @SuppressWarnings("unchecked")
    void whenGetTemperature_andApiReturnsNull_shouldReturnNullString() {
        // --- Arrange ---
        double latitude = 52.52;
        double longitude = 13.41;

        // Mock the RestClient to return null (simulating unexpected API response)
        RestClient.RequestHeadersUriSpec mockSpec = mock(RestClient.RequestHeadersUriSpec.class);
        when(restClient.get()).thenReturn(mockSpec);
        when(mockSpec.uri(anyString(), any(Object.class), any(Object.class))).thenReturn(mockSpec);
        when(mockSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(WeatherResponse.class)).thenReturn(null);

        // --- Act ---
        String result = weatherService.getTemperature(latitude, longitude, toolContext);

        // --- Assert ---
        // ModelOptionsUtils.toJsonStringPrettyPrinter handles null gracefully and returns "null" as string
        assertNotNull(result, "Result should not be null");
        assertTrue(result.equals("null") || result.contains("null"),
                "Result should indicate null response");

        // --- Verify ---
        verify(restClient, times(1)).get();
        verify(mockSpec, times(1)).uri(anyString(), eq(latitude), eq(longitude));
        verify(mockSpec, times(1)).retrieve();
        verify(responseSpec, times(1)).body(WeatherResponse.class);
    }

    // =========================================================================
    // Additional Edge Case Tests
    // =========================================================================

    @Test
    @DisplayName("getTemperature - Edge case: Zero coordinates (equator and prime meridian)")
    @SuppressWarnings("unchecked")
    void whenGetTemperature_withZeroCoordinates_shouldReturnWeatherData() {
        // --- Arrange ---
        double latitude = 0.0;
        double longitude = 0.0;
        LocalDateTime currentTime = LocalDateTime.now();

        Current mockCurrent = new Current(currentTime, 900, 20.0);
        WeatherResponse mockWeatherResponse = new WeatherResponse(mockCurrent);

        RestClient.RequestHeadersUriSpec mockSpec = mock(RestClient.RequestHeadersUriSpec.class);
        when(restClient.get()).thenReturn(mockSpec);
        when(mockSpec.uri(anyString(), any(Object.class), any(Object.class))).thenReturn(mockSpec);
        when(mockSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(WeatherResponse.class)).thenReturn(mockWeatherResponse);

        // --- Act ---
        String result = weatherService.getTemperature(latitude, longitude, toolContext);

        // --- Assert ---
        assertNotNull(result, "Result should not be null");
        assertTrue(result.contains("20.0"), "Result should contain temperature");

        // --- Verify ---
        verify(mockSpec, times(1)).uri(anyString(), eq(0.0), eq(0.0));
    }

    @Test
    @DisplayName("getTemperature - Edge case: Negative temperature (Arctic region)")
    @SuppressWarnings("unchecked")
    void whenGetTemperature_withNegativeTemperature_shouldReturnWeatherData() {
        // --- Arrange ---
        double latitude = 70.0;  // Arctic region
        double longitude = 25.0;
        LocalDateTime currentTime = LocalDateTime.now();
        double negativeTemperature = -25.3;

        Current mockCurrent = new Current(currentTime, 900, negativeTemperature);
        WeatherResponse mockWeatherResponse = new WeatherResponse(mockCurrent);

        RestClient.RequestHeadersUriSpec mockSpec = mock(RestClient.RequestHeadersUriSpec.class);
        when(restClient.get()).thenReturn(mockSpec);
        when(mockSpec.uri(anyString(), any(Object.class), any(Object.class))).thenReturn(mockSpec);
        when(mockSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(WeatherResponse.class)).thenReturn(mockWeatherResponse);

        // --- Act ---
        String result = weatherService.getTemperature(latitude, longitude, toolContext);

        // --- Assert ---
        assertNotNull(result, "Result should not be null");
        assertTrue(result.contains("-25.3"), "Result should contain negative temperature");
        assertTrue(result.contains("temperature_2m"), "Result should contain temperature field");

        // --- Verify ---
        verify(responseSpec, times(1)).body(WeatherResponse.class);
    }

    @Test
    @DisplayName("getTemperature - Edge case: Extreme coordinates (South Pole)")
    @SuppressWarnings("unchecked")
    void whenGetTemperature_withExtremeCoordinates_shouldReturnWeatherData() {
        // --- Arrange ---
        double latitude = -90.0;  // South Pole
        double longitude = 0.0;
        LocalDateTime currentTime = LocalDateTime.now();

        Current mockCurrent = new Current(currentTime, 900, -40.0);
        WeatherResponse mockWeatherResponse = new WeatherResponse(mockCurrent);

        RestClient.RequestHeadersUriSpec mockSpec = mock(RestClient.RequestHeadersUriSpec.class);
        when(restClient.get()).thenReturn(mockSpec);
        when(mockSpec.uri(anyString(), any(Object.class), any(Object.class))).thenReturn(mockSpec);
        when(mockSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(WeatherResponse.class)).thenReturn(mockWeatherResponse);

        // --- Act ---
        String result = weatherService.getTemperature(latitude, longitude, toolContext);

        // --- Assert ---
        assertNotNull(result, "Result should not be null");
        assertTrue(result.contains("-40.0"), "Result should contain extreme cold temperature");

        // --- Verify ---
        verify(mockSpec, times(1)).uri(anyString(), eq(-90.0), eq(0.0));
    }
}