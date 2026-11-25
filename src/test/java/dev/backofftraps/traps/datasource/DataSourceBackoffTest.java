package dev.backofftraps.traps.datasource;

import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A second {@link DataSource} bean removes the primary one together with its pool settings.
 * H2 in memory, no connections are opened: the tests check what is wired.
 */
class DataSourceBackoffTest {

    private static final String MAIN_URL = "jdbc:h2:mem:main";
    private static final String MAIN_POOL = "main-pool";

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(DataSourceAutoConfiguration.class))
            .withPropertyValues(
                    "spring.datasource.url=" + MAIN_URL,
                    "spring.datasource.hikari.pool-name=" + MAIN_POOL);

    @Test
    @DisplayName("Boot's data source: built from spring.datasource.*")
    void bootCreatesPrimaryDataSource() {
        runner.run(context -> {
            var dataSource = context.getBean(DataSource.class);

            assertThat(dataSource).isInstanceOf(HikariDataSource.class);
            assertThat(((HikariDataSource) dataSource).getJdbcUrl()).isEqualTo(MAIN_URL);
            assertThat(((HikariDataSource) dataSource).getPoolName()).isEqualTo(MAIN_POOL);
        });
    }

    @Test
    @DisplayName("TRAP: a reporting data source, the primary one and its pool settings are gone")
    void secondDataSourceRemovesPrimary() {
        runner.withUserConfiguration(ReportingDataSourceTrap.class).run(context -> {
            assertThat(context).hasSingleBean(DataSource.class);

            var dataSource = (HikariDataSource) context.getBean(DataSource.class);
            assertThat(dataSource.getJdbcUrl()).isEqualTo(ReportingDataSourceTrap.URL);
            assertThat(dataSource.getPoolName()).isNotEqualTo(MAIN_POOL);
        });
    }

    @Test
    @DisplayName("SAFE: defaultCandidate = false, the reporting source exists by name, the primary stays")
    void nonDefaultSecondDataSourceKeepsPrimary() {
        runner.withUserConfiguration(NonDefaultReportingDataSourceConfig.class).run(context -> {
            var primary = (HikariDataSource) context.getBean(DataSource.class);
            var reporting = (HikariDataSource) context.getBean("reportingDataSource");

            assertThat(primary.getJdbcUrl()).isEqualTo(MAIN_URL);
            assertThat(primary.getPoolName()).isEqualTo(MAIN_POOL);
            assertThat(reporting.getJdbcUrl()).isEqualTo(ReportingDataSourceTrap.URL);
        });
    }
}
