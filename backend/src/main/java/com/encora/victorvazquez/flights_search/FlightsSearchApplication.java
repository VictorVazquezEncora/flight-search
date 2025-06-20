package com.encora.victorvazquez.flights_search;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

import com.encora.victorvazquez.flights_search.config.AmadeusProperties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootApplication
@EnableConfigurationProperties(AmadeusProperties.class)
@ComponentScan(basePackages = "com.encora.victorvazquez.flights_search")
public class FlightsSearchApplication {
    
    private static final Logger logger = LoggerFactory.getLogger(FlightsSearchApplication.class);

    public static void main(String[] args) {
        try {
            logger.info("Starting Flights Search Application...");
            SpringApplication app = new SpringApplication(FlightsSearchApplication.class);
            app.run(args);
            logger.info("Flights Search Application started successfully");
        } catch (Exception e) {
            if (e.getClass().getName().contains("SilentExit")) {
                logger.debug("DevTools restarting the application...");
                return;
            }
            logger.error("Failed to start Flights Search Application", e);
            throw e;
        }
    }
}
