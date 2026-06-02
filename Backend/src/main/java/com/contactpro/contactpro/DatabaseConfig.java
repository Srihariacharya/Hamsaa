package com.contactpro.contactpro;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class DatabaseConfig {

    @Bean
    public DataSource dataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        // Hardcode the external Render URL to forcefully bypass any broken environment variables
        dataSource.setJdbcUrl("jdbc:postgresql://dpg-d8fe0128qa3s738tl1cg-a.oregon-postgres.render.com:5432/hamsaa_db_new?sslmode=require");
        dataSource.setUsername("hamsaa_db_new_user");
        dataSource.setPassword("1ZNi9fEKlNSL2iGX1hVaRaTBa4wtsTiN");
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setMaximumPoolSize(3);
        return dataSource;
    }
}
