package com.mcp.sample.server.service;

import java.time.LocalDateTime;
import org.slf4j.Logger;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class WeatherService {

	private static final Logger logger = org.slf4j.LoggerFactory.getLogger(WeatherService.class);

	private final RestClient restClient;

	public WeatherService() {
		this.restClient = RestClient.create();
	}

	/**
	 * The response format from the Open-Meteo API
	 */
	public record WeatherResponse(Current current) {
		public record Current(LocalDateTime time, int interval, double temperature_2m) {
		}
	}

	@Tool(description = "Get the temperature (in celsius) for a specific location")
	public String getTemperature(@ToolParam(description = "The location latitude") double latitude,
			@ToolParam(description = "The location longitude") double longitude,
			ToolContext toolContext) {

		WeatherResponse weatherResponse = restClient
				.get()
				.uri("https://api.open-meteo.com/v1/forecast?latitude={latitude}&longitude={longitude}&current=temperature_2m",
						latitude, longitude)
				.retrieve()
				.body(WeatherResponse.class);

		String response = ModelOptionsUtils.toJsonStringPrettyPrinter(weatherResponse);

		return response;
	}

}