package com.billing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class BillingBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BillingBackendApplication.class, args);
    }
}
