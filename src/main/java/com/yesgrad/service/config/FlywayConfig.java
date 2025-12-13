package com.yesgrad.service.config;

import org.flywaydb.core.Flyway;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class FlywayConfig {

    @Bean(initMethod = "migrate")
    public Flyway flyway(Environment env) {
        return Flyway.configure()
                .dataSource(
                        env.getRequiredProperty("spring.datasource.url"),
                        env.getRequiredProperty("spring.datasource.username"),
                        env.getRequiredProperty("spring.datasource.password")
                )
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .baselineVersion("8")
                .outOfOrder(true)
                .load();
    }
}
