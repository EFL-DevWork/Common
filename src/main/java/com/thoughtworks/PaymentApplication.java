package com.thoughtworks;

import com.thoughtworks.config.YamlPropertySourceFactory;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication
@PropertySource(value = "classpath:auth0.yml", factory = YamlPropertySourceFactory.class)
@PropertySource(value = "classpath:keycloak.yml", factory = YamlPropertySourceFactory.class)
@PropertySource(value = "classpath:security-common.yml", factory = YamlPropertySourceFactory.class)
public class PaymentApplication {
    public static void main(String[] args) {
        Dotenv.configure()
                .ignoreIfMissing()
                .systemProperties()
                .load();
        SpringApplication.run(PaymentApplication.class, args);
    }
}
