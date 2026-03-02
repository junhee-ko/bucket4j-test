package me.jko.bucket4jtest

import io.github.bucket4j.Bucket
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class RateLimitConfig {

    val greedyBucket: Bucket =
        Bucket.builder()
            .addLimit { limit ->
                limit.capacity(10)
                    .refillGreedy(10, Duration.ofSeconds(100))
            }
            .build()

    val intervallyBucket: Bucket =
        Bucket.builder()
            .addLimit { limit ->
                limit.capacity(10)
                    .refillIntervally(10, Duration.ofSeconds(100))
            }
            .build()
}