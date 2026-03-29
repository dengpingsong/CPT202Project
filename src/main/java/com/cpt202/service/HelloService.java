package com.cpt202.service;

import org.springframework.stereotype.Service;

/**
 * Simple greeting service used to demonstrate the service layer.
 */
@Service
public class HelloService {

    /**
     * Return a personalised greeting.
     *
     * @param name the name to greet; defaults to "World" when blank
     * @return greeting string
     */
    public String greet(String name) {
        String target = (name == null || name.isBlank()) ? "World" : name;
        return "Hello, " + target + "!";
    }
}
