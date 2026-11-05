package com.example.food_delivery_app.order.assignment.strategy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration // Spring Boot will automatically detect this class and register the beans defined in it.
// Why not use @Component? Because this class is not a component, it is a configuration class that defines beans. @Configuration is more appropriate here.
public class DriverAssignmentConfig {

    // application.yaml:  delivery.assignment-strategy: nearest   (or: least-busy)
    @Bean
    public DriverAssignmentStrategy driverAssignmentStrategy(
            @Value("${delivery.assignment-strategy:nearest}") String name) {
        return switch (name) {
            case "nearest" -> new NearestDriverStrategy();
            case "least-busy" -> new LeastBusyDriverStrategy();
            default -> throw new IllegalArgumentException("unknown delivery.assignment-strategy: " + name);
        };
    }
}