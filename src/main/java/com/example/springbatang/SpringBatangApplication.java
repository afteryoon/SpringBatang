package com.example.springbatang;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class SpringBatangApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBatangApplication.class, args);
    }

}
