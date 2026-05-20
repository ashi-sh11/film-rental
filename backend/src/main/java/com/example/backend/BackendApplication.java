package com.example.backend;

import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@SpringBootApplication
public class BackendApplication {

    private static final Logger log = LoggerFactory.getLogger(BackendApplication.class);

    public static void main(String[] args) {

        System.setProperty("spring.devtools.restart.enabled", "false");

        loadDotenv();
        SpringApplication.run(BackendApplication.class, args);
    }

    private static void loadDotenv() {
        Path[] candidates = {
                Paths.get(".env"),
                Paths.get("backend", ".env"),
                Paths.get("..", "backend", ".env")
        };

        for (Path candidate : candidates) {
            Path abs = candidate.toAbsolutePath().normalize();
            if (Files.isRegularFile(abs)) {
                Dotenv dotenv = Dotenv.configure()
                        .directory(abs.getParent().toString())
                        .filename(abs.getFileName().toString())
                        .ignoreIfMissing()
                        .load();
                dotenv.entries().forEach(e -> System.setProperty(e.getKey(), e.getValue()));
                log.info("Loaded {} variables from {}", dotenv.entries().size(), abs);
                return;
            }
        }

        log.warn("No .env file found in: {}, {}, or {} (CWD: {})",
                candidates[0].toAbsolutePath(),
                candidates[1].toAbsolutePath(),
                candidates[2].toAbsolutePath().normalize(),
                Paths.get("").toAbsolutePath());
    }
}
