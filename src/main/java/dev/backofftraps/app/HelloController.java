package dev.backofftraps.app;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * A protected endpoint. Security tests check who can reach it.
 */
@RestController
public class HelloController {

    public static final String PATH = "/api/hello";

    @GetMapping(PATH)
    public String hello() {
        return "hello";
    }
}
