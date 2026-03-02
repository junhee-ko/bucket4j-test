package me.jko.bucket4jtest

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/test")
class RateLimitController(
    private val rateLimitConfig: RateLimitConfig
) {

    @GetMapping("/greedy")
    fun greedy(): ResponseEntity<String> =
        if (rateLimitConfig.greedyBucket.tryConsume(1)) {
            ResponseEntity.ok("Greedy ok, 남은 토큰: ${rateLimitConfig.greedyBucket.availableTokens}")
        } else {
            ResponseEntity
                .status(HttpStatus.TOO_MANY_REQUESTS)
                .body("Greedy Rate limit exceeded")
        }

    @GetMapping("/intervally")
    fun intervally(): ResponseEntity<String> =
        if (rateLimitConfig.intervallyBucket.tryConsume(1)) {
            ResponseEntity.ok("Intervally ok, 남은 토큰: ${rateLimitConfig.intervallyBucket.availableTokens}")
        } else {
            ResponseEntity
                .status(HttpStatus.TOO_MANY_REQUESTS)
                .body("Intervally Rate limit exceeded")
        }
}