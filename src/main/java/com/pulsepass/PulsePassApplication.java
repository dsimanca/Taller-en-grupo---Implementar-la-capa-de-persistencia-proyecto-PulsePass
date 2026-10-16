package com.pulsepass;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class PulsePassApplication {
    public static void main(String[] args) {
        SpringApplication.run(PulsePassApplication.class, args);
    }
}
