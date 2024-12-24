package com.supersoft.oneapi.init;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Database initializer
 */
@Component
@Slf4j
public class OneapiDatabaseInitializer implements CommandLineRunner {
    private static final String H2 = "H2";
    private static final String MYSQL = "MySQL";
    private static final String H2_TEST_SQL = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'ONEAPI_MODEL'";
    private static final String MYSQL_TEST_SQL = "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'oneapi_model'";

    @Value("classpath:sql/init.h2.sql")
    private Resource initH2Sql;

    @Value("classpath:sql/init.mysql.sql")
    private Resource initMysqlSql;

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    public OneapiDatabaseInitializer(DataSource dataSource) {
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public void run(String... args) throws Exception {
        boolean initialized = isDatabaseInitialized();
        if (initialized) {
            log.info("Database already initialized.");
            return;
        }
        String driverName = dataSource.getConnection().getMetaData().getDriverName();
        Resource sqlResource;
        if (driverName.contains(H2)) {
            sqlResource = initH2Sql;
        } else if (driverName.contains(MYSQL)) {
            sqlResource = initMysqlSql;
        } else {
            throw new UnsupportedOperationException("Unsupported database: " + driverName);
        }
        initializeDatabase(sqlResource);
    }

    private boolean isDatabaseInitialized() {
        try {
            String driverName = dataSource.getConnection().getMetaData().getDriverName();
            String testSql = driverName.contains(H2) ? H2_TEST_SQL : MYSQL_TEST_SQL;
            Integer count = jdbcTemplate.queryForObject(testSql, Integer.class);
            return count != null && count > 0;
        } catch (Exception e) {
            log.error("Failed to check if database is initialized.", e);
            return false;
        }
    }

    private void initializeDatabase(Resource sqlResource) throws Exception {
        try (InputStreamReader in = new InputStreamReader(sqlResource.getInputStream());
                BufferedReader reader = new BufferedReader(in)) {
            String sql = reader.lines().collect(Collectors.joining("\n"));
            jdbcTemplate.execute(sql);
            log.info("Database initialized successfully.");
        } catch (Exception e) {
            log.error("Database initialization failed.", e);
            throw e;
        }
    }
}
