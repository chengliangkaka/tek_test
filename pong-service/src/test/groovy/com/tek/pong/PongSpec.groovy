package com.tek.pong

import com.tek.pong.controller.PongController
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import spock.lang.Specification

/**
 * @author chengliangpu* @date 2024/11/10
 */
@SpringBootTest
class PongSpec extends Specification{
    @Autowired
    PongController pongController

    def "Should respond with 'World' when request is within limit"() {
        given:

        when:
        def response = this.pongController.getPongResponse("Hello").block()

        then:
        response.getStatusCode() == HttpStatus.OK
        response.getBody() == "World"
    }

    def "Should respond with 429 when limit exceeded"() {
        given:

        when:
        this.pongController.getPongResponse("Hello").block()
        def response = this.pongController.getPongResponse("Hello").block()

        then:
        response.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS
    }
}
