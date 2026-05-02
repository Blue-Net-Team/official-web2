package com.bluenet.judge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Judge Service 启动入口。
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class JudgeServiceApplication {
    /**
     * 启动 Judge Service。
     *
     * @param args
     *            命令行参数。
     */
    public static void main(String[] args) {
        SpringApplication.run(JudgeServiceApplication.class, args);
    }
}
