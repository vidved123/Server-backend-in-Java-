package org.library.controller;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api")
public class UserController {

    @GetMapping("/usernames")
    public List<String> getUsernames(@RequestParam String query) {
        // TODO: Replace this with real user lookup logic
        return Stream.of("vidhyut", "vidit", "vidhya", "vidur")
                .filter(name -> name.toLowerCase().startsWith(query.toLowerCase()))
                .toList();
    }
}
