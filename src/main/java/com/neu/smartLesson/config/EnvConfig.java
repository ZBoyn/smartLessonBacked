package com.neu.smartLesson.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import java.util.Properties;

@Configuration(proxyBeanMethods = false)
public class EnvConfig {
    @Bean
    public Dotenv dotenv() {
        return Dotenv.configure()
                .directory("./") // 指向根目录
                .ignoreIfMalformed()
                .ignoreIfMissing()
                .load();
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer dotenvPropertySource(Dotenv dotenv) {
        Properties props = new Properties();
        dotenv.entries().forEach(entry -> props.setProperty(entry.getKey(), entry.getValue()));

        PropertySourcesPlaceholderConfigurer configurer = new PropertySourcesPlaceholderConfigurer();
        configurer.setProperties(props);
        return configurer;
    }
}