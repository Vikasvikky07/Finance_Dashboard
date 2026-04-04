package com.Zorvyn.Finance_Dashboard.service;

import com.Zorvyn.Finance_Dashboard.exception.TooManyRequestsException;
import java.time.Duration;
import java.time.Instant;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import org.springframework.stereotype.Service;

@Service
public class LoginRateLimiter {

    private static final int MAX_REQUESTS_PER_WINDOW = 5;
    private static final Duration WINDOW = Duration.ofMinutes(1);

    private final ConcurrentHashMap<String, Deque<Instant>> attempts = new ConcurrentHashMap<>();

    public void validate(String key) {
        Deque<Instant> requestTimes = attempts.computeIfAbsent(normalizeKey(key), unused -> new ConcurrentLinkedDeque<>());
        Instant threshold = Instant.now().minus(WINDOW);

        while (!requestTimes.isEmpty() && requestTimes.peekFirst().isBefore(threshold)) {
            requestTimes.pollFirst();
        }

        if (requestTimes.size() >= MAX_REQUESTS_PER_WINDOW) {
            throw new TooManyRequestsException("Too many login attempts. Please try again later.");
        }

    }

    public void recordFailure(String key) {
        attempts.computeIfAbsent(normalizeKey(key), unused -> new ConcurrentLinkedDeque<>())
                .addLast(Instant.now());
    }

    public void recordSuccess(String key) {
        attempts.remove(normalizeKey(key));
    }

    private String normalizeKey(String key) {
        return key == null ? "anonymous" : key.trim().toLowerCase();
    }
}
