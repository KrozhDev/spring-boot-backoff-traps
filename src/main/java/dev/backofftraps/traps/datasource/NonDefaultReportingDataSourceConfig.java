package dev.backofftraps.traps.datasource;

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * SAFE. {@code defaultCandidate = false}: the reporting data source is injected only by name or
 * qualifier, Boot still creates the primary one from {@code spring.datasource.*}.
 */
@Configuration(proxyBeanMethods = false)
public class NonDefaultReportingDataSourceConfig {

    @Bean(defaultCandidate = false)
    DataSource reportingDataSource() {
        return DataSourceBuilder.create().url(ReportingDataSourceTrap.URL).build();
    }
}
