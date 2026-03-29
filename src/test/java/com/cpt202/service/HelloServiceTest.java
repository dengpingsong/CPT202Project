package com.cpt202.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link HelloService}.
 * No Spring context is started – pure JUnit 5 tests.
 */
class HelloServiceTest {

    private HelloService helloService;

    @BeforeEach
    void setUp() {
        helloService = new HelloService();
    }

    @Test
    @DisplayName("greet with a name returns personalised greeting")
    void greet_withName_returnsPersonalisedGreeting() {
        String result = helloService.greet("Alice");
        assertThat(result).isEqualTo("Hello, Alice!");
    }

    @Test
    @DisplayName("greet with null defaults to 'World'")
    void greet_withNull_defaultsToWorld() {
        String result = helloService.greet(null);
        assertThat(result).isEqualTo("Hello, World!");
    }

    @Test
    @DisplayName("greet with blank string defaults to 'World'")
    void greet_withBlank_defaultsToWorld() {
        String result = helloService.greet("   ");
        assertThat(result).isEqualTo("Hello, World!");
    }
}
