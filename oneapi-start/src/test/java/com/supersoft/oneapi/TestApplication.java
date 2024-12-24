package com.supersoft.oneapi;

import com.supersoft.oneapi.test.TestContainer;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.supersoft.oneapi"})
@MapperScan({"com.supersoft.oneapi.*.mapper"})
public class TestApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestApplication.class, args);
        TestContainer.start();
    }
}
