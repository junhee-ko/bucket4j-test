package me.jko.bucket4jtest

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@SpringBootTest
@AutoConfigureMockMvc
class RateLimitControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var rateLimitConfig: RateLimitConfig

    @BeforeEach
    fun setUp() {
        rateLimitConfig.greedyBucket.reset()
        rateLimitConfig.intervallyBucket.reset()
    }

    /*
    * greedy
    * */
    @Test
    fun `greedy - 10번 요청은 모두 성공해야 한다`() {
        repeat(10) {
            mockMvc.get("/test/greedy")
                .andExpect { status { isOk() } }
        }
    }

    @Test
    fun `greedy - 11번째 요청은 429를 반환해야 한다`() {
        repeat(10) {
            mockMvc.get("/test/greedy")
        }

        mockMvc.get("/test/greedy")
            .andExpect { status { isTooManyRequests() } }
    }

    @Test
    fun `greedy - 50초 후에는 부분 리필되어 요청이 성공해야 한다`() {
        // 10개 모두 소진
        repeat(10) {
            mockMvc.get("/test/greedy")
        }

        // 50초 대기 (100초에 10개 → 50초에 5개 리필)
        Thread.sleep(50_000)

        // 5번은 성공
        repeat(5) {
            mockMvc.get("/test/greedy")
                .andExpect { status { isOk() } }
        }

        // 6번째는 실패
        mockMvc.get("/test/greedy")
            .andExpect { status { isTooManyRequests() } }
    }

    /*
    * intervally
    * */
    @Test
    fun `intervally - 10번 요청은 모두 성공해야 한다`() {
        repeat(10) {
            mockMvc.get("/test/intervally")
                .andExpect { status { isOk() } }
        }
    }

    @Test
    fun `intervally - 11번째 요청은 429를 반환해야 한다`() {
        repeat(10) {
            mockMvc.get("/test/intervally")
        }

        mockMvc.get("/test/intervally")
            .andExpect { status { isTooManyRequests() } }
    }

    @Test
    fun `intervally - 50초 후에는 리필되지 않아 요청이 실패해야 한다`() {
        // 10개 모두 소진
        repeat(10) {
            mockMvc.get("/test/intervally")
        }

        // 30초 대기 (아직 1주기 미완료)
        Thread.sleep(50_000)

        // Greedy 와 달리 여전히 실패
        mockMvc.get("/test/intervally")
            .andExpect { status { isTooManyRequests() } }
    }

    /*
    * Greedy vs Intervally
    * */
    @Test
    fun `50초 후 greedy 는 성공하고 intervally 는 실패한다`() {
        // 둘 다 소진
        repeat(10) {
            mockMvc.get("/test/greedy")
            mockMvc.get("/test/intervally")
        }

        Thread.sleep(50_000)

        // Greedy → 성공 (부분 리필)
        mockMvc.get("/test/greedy")
            .andExpect { status { isOk() } }

        // Intervally → 실패 (1주기 미완료)
        mockMvc.get("/test/intervally")
            .andExpect { status { isTooManyRequests() } }
    }
}