package com.tek.ping

import com.tek.ping.constant.SendStateConstant
import com.tek.ping.service.PingService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor
import spock.lang.Specification

import java.nio.channels.FileChannel
import java.nio.channels.FileLock
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption

@SpringBootTest
@MockBean(ScheduledAnnotationBeanPostProcessor)
// disable to open schedule task
class PingSpec extends Specification {
    @Autowired
    PingService pingService

    def "Should respond success when send first request and lock file not create"() {
        given:

        when:
        Files.delete(Paths.get("/tmp/ping_rate_limit.lock"))
        def state = this.pingService.sendPing()

        then:
        state == SendStateConstant.SUCCESS
    }

    def "Should respond file locked when send request after file has been locked"() {
        given:

        when:
        FileChannel channel = FileChannel.open(Paths.get("/tmp/ping_rate_limit.lock"), StandardOpenOption.CREATE, StandardOpenOption.READ, StandardOpenOption.WRITE)
        FileLock lock = channel.tryLock();
        def state = this.pingService.sendPing()

        then:
        state == SendStateConstant.CANNOT_GET_LOCK
        lock.release()
        channel.close()
    }

    def "Should respond success when send first request"() {
        given:

        when:
        def state = this.pingService.sendPing()

        then:
        state == SendStateConstant.SUCCESS
    }

    def "Should respond success when send two request continuously"() {
        given:

        when:
        long remain = System.currentTimeMillis() % 1000;
        long duration = 1000 - remain
        if (duration > 0)
            Thread.sleep(duration)
        this.pingService.sendPing()
        def state = this.pingService.sendPing()

        then:
        state == SendStateConstant.SUCCESS
    }

    def "Should respond limit exceeded when send three request continuously"() {
        given:

        when:
        long remain = System.currentTimeMillis() % 1000;
        long duration = 1000 - remain
        if (duration > 0)
            Thread.sleep(duration)
        this.pingService.sendPing()
        this.pingService.sendPing()
        def state = this.pingService.sendPing()

        then:
        state == SendStateConstant.RATE_LIMIT_TIMES
    }

    def "Should respond sucess when start a new request of a new second after send three request continuously"() {
        given:

        when:
        long remain = System.currentTimeMillis() % 1000;
        long duration = 1000 - remain
        if (duration > 0)
            Thread.sleep(duration)
        this.pingService.sendPing()
        this.pingService.sendPing()
        this.pingService.sendPing()
        Thread.sleep(1000)
        def state = this.pingService.sendPing()

        then:
        state == SendStateConstant.SUCCESS
    }
}
