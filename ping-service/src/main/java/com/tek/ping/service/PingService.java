package com.tek.ping.service;

import com.tek.ping.constant.SendStateConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

@Service
public class PingService {
    @Autowired
    private WebClient webClientWithPong;
    private final Logger logger = LoggerFactory.getLogger(PingService.class);
    private static final Integer RATE_LIMIT_TIMES = 2;

    @Scheduled(fixedRate = 1000)
    public int sendPing() {
        try (FileChannel channel = FileChannel.open(Paths.get("/tmp/ping_rate_limit.lock"), StandardOpenOption.CREATE, StandardOpenOption.READ, StandardOpenOption.WRITE)) {
            FileLock lock = channel.tryLock();
            if (lock == null) {
                logger.error("Could not acquire file lock");
                return SendStateConstant.CANNOT_GET_LOCK;
            }

            try {
                ByteBuffer buffer = ByteBuffer.allocate(12); // 8+4
                int size = channel.read(buffer);
                Long currentTimestamp = System.currentTimeMillis() / 1000;
                if (size <= 0) {
                    this.sendReqeust();
                    // first insert
                    this.saveRateLimitState(channel, buffer, currentTimestamp, 1);
                    return SendStateConstant.SUCCESS;
                }

                buffer.flip();
                Long lastTimestamp = buffer.getLong(); // get last record timestamp
                int times = buffer.getInt(); // get times per second
                if (currentTimestamp.equals(lastTimestamp)) {
                    if (times >= RATE_LIMIT_TIMES) {
                        logger.error("Request not sent as being rate limited over {} times per second", RATE_LIMIT_TIMES);
                        return SendStateConstant.RATE_LIMIT_TIMES;
                    } else {
                        this.sendReqeust();
                        // update times
                        this.saveRateLimitState(channel, buffer, currentTimestamp, ++times);
                        return SendStateConstant.SUCCESS;
                    }
                } else {
                    this.sendReqeust();
                    // insert new
                    this.saveRateLimitState(channel, buffer, currentTimestamp, 1);
                    return SendStateConstant.SUCCESS;
                }
            } catch (IOException e) {
                logger.error("read or write content file error", e);
            } finally {
                lock.release(); // release the file lock
            }
        } catch (Exception e) {
            logger.error("Could not acquire file lock", e);
            return SendStateConstant.CANNOT_GET_LOCK;
        }
        return SendStateConstant.UNEXPECTED_EXCEPTION;
    }

    private void saveRateLimitState(FileChannel channel, ByteBuffer buffer, Long timestamp, Integer times) throws IOException {
        // update timestamp and request times per second
        buffer.clear();
        buffer.putLong(timestamp);
        buffer.putInt(times);
        buffer.flip();
        channel.position(0);
        channel.write(buffer);
        channel.force(false);
    }

    private void sendReqeust() {
        this.webClientWithPong.get().retrieve()
                .bodyToMono(String.class)
                .doOnNext(response -> {
                    logger.info("Request sent. Pong responded with: " + response);
                })
                .doOnError(error -> {
                    logger.info("Request sent. Pong throttled it with 429");
                })
                .subscribe();
    }
}
