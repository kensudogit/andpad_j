package jp.andpad.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import jp.andpad.api.config.AppProperties;

@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
public class AndpadApplication {

    public static void main(String[] args) {
        SpringApplication.run(AndpadApplication.class, args);
    }
}
