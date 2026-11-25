package dev.backofftraps.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Scans only {@code dev.backofftraps.app}: the traps live in {@code dev.backofftraps.traps}
 * and are switched on one by one in tests, never by component scan.
 */
@SpringBootApplication
public class TrapsApplication {

    public static void main(String[] args) {
        SpringApplication.run(TrapsApplication.class, args);
    }
}
