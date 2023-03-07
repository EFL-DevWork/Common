package com.thoughtworks.config;

import com.thoughtworks.CryptoConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
public class CryptoConfig {
    @Autowired
    private ApplicationContext applicationContext;
    private static ApplicationContext _applicationContext;
    private static CryptoConverter cryptoConverter;

    @Bean
    public CryptoConverter cryptoConverter(@Value("${key}") String key, @Value("${db.encryption.algorithm:AES}") String algorithm) {
        return new CryptoConverter(key, algorithm);
    }

    @PostConstruct
    public void initialize(){
        setApplicationContext();
    }

    private void setApplicationContext() {
        _applicationContext = applicationContext;
    }

    public static CryptoConverter get(){
        if (cryptoConverter == null){
            cryptoConverter = _applicationContext.getBean(CryptoConverter.class);
        }
        return cryptoConverter;
    }

    

}
