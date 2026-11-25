package dev.backofftraps.traps.datasource;

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * TRAP. A second data source for reports.
 * <p>
 * Boot creates the primary data source from {@code spring.datasource.*} only while no
 * {@link DataSource} bean exists. After this bean appears the primary one and its pool settings
 * are gone, and repositories quietly talk to the reporting database.
 */
@Configuration(proxyBeanMethods = false)
public class ReportingDataSourceTrap {

    public static final String URL = "jdbc:h2:mem:reporting";

    @Bean
    DataSource reportingDataSource() {
        return DataSourceBuilder.create().url(URL).build();
    }
}
