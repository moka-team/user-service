package com.mokaform.userservice.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import redis.embedded.RedisServer;


import javax.annotation.*;
import java.io.IOException;

@Profile(value = "local")
@Configuration
public class EmbeddedRedisConfig {

    @Value("${spring.redis.port}")
    private int port;

    private RedisServer redisServer;

    @PostConstruct
    public void redisServer() throws IOException {
        redisServer = new RedisServer(port);
        redisServer.start();
    }

    @PreDestroy
    public void stopRedis() {
        if (redisServer != null) {
            redisServer.stop();
        }
    }

    /**
     * 랜덤 포트 설정 linux, mac에서만 동작
     */

//    @PostConstruct
//    public void redisServer() throws IOException {
//        redisServer = new RedisServer(isRedisRunning() ? findAvailablePort() : port);
//        redisServer.start();
//    }
//
//    @PreDestroy
//    public void stopRedis() {
//        if (redisServer != null) {
//            redisServer.stop();
//        }
//    }
//
//    /**
//     * Embedded Redis가 현재 실행중인지 확인
//     */
//    private boolean isRedisRunning() throws IOException {
//        return isRunning(executeGrepProcessCommand(port));
//    }
//
//    /**
//     * 현재 PC/서버에서 사용가능한 포트 조회
//     */
//    public int findAvailablePort() throws IOException {
//
//        for (int port = 10000; port <= 65535; port++) {
//            Process process = executeGrepProcessCommand(port);
//            if (!isRunning(process)) {
//                return port;
//            }
//        }
//
//        throw new IllegalArgumentException("Not Found Available port: 10000 ~ 65535");
//    }
//
//    /**
//     * 해당 port를 사용중인 프로세스 확인하는 sh 실행
//     */
//    private Process executeGrepProcessCommand(int port) throws IOException {
//        String command = String.format("netstat -nat | grep LISTEN|grep %d", port);
//        String[] shell = {"/bin/sh", "-c", command};
//        return Runtime.getRuntime().exec(shell);
//    }
//
//    /**
//     * 해당 Process가 현재 실행중인지 확인
//     */
//    private boolean isRunning(Process process) {
//        String line;
//        StringBuilder pidInfo = new StringBuilder();
//
//        try (BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
//
//            while ((line = input.readLine()) != null) {
//                pidInfo.append(line);
//            }
//
//        } catch (Exception e) {
//        }
//
//        return StringUtils.hasText(pidInfo.toString());
//    }
}