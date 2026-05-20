package com.example.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootApplication
public class BackendApplication {

    private static final Logger log = LoggerFactory.getLogger(BackendApplication.class);

    public static void main(String[] args) {

        // Must be set BEFORE SpringApplication.run() — DevTools' RestartApplicationListener
        // reads this very early, and the equivalent line in application.properties is
        // processed too late. Without this, the RestartClassLoader stays active and
        // every JDK-serialized Redis cache hit fails with ClassCastException
        // ("Cannot cast Foo to Foo"), which surfaces as the cached endpoint returning
        // empty/broken data on the second click.
        System.setProperty("spring.devtools.restart.enabled", "false");

        try {
            Dotenv dotenv = Dotenv.configure()
                    .directory(".")
                    .ignoreIfMissing()
                    .load();
            dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));
        } catch (Exception e) {
            log.warn("Failed to load .env file: {}", e.getMessage());
        }

        SpringApplication.run(BackendApplication.class, args);
    }

}