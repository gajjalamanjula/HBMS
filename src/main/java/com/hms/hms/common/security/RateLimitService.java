package com.hms.hms.common.security;

import com.hms.hms.common.exception.CustomException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitService {

    private static final int MAX_ATTEMPTS = 5;
    private static final int WINDOW_MINUTES = 15;

    private final Map<String, AttemptInfo> attempts =
            new ConcurrentHashMap<>();

    public void checkLimit(String key) {

        LocalDateTime now = LocalDateTime.now();

        AttemptInfo attemptInfo = attempts.get(key);

        if (attemptInfo == null) {

            attempts.put(
                    key,
                    new AttemptInfo(1, now)
            );

            return;
        }

        if (attemptInfo.getFirstAttempt()
                .plusMinutes(WINDOW_MINUTES)
                .isBefore(now)) {

            attempts.put(
                    key,
                    new AttemptInfo(1, now)
            );

            return;
        }

        if (attemptInfo.getCount() >= MAX_ATTEMPTS) {

            throw new CustomException(
                    "Too many attempts. Please try again after 15 minutes.",
                    HttpStatus.TOO_MANY_REQUESTS
            );
        }

        attemptInfo.setCount(
                attemptInfo.getCount() + 1
        );
    }

    public void reset(String key) {
        attempts.remove(key);
    }

    private static class AttemptInfo {

        private int count;

        private LocalDateTime firstAttempt;

        public AttemptInfo(
                int count,
                LocalDateTime firstAttempt
        ) {
            this.count = count;
            this.firstAttempt = firstAttempt;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public LocalDateTime getFirstAttempt() {
            return firstAttempt;
        }
    }
}