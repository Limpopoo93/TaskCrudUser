package by.config;

import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;

import java.util.Objects;

@Configuration
@PropertySource("classpath:database.properties")
public class DatabaseConfig {
    @Value("${driverName}")
    private String driverClassName;

    @Value("${password}")
    private String password;

    @Value("${url}")
    private String url;

    @Value("${initialSize}")
    private String initialSize;

    @Value("${login}")
    private String username;

    @Value("${maxActive}")
    private String maxActive;

    @Bean(value = "dataSource", destroyMethod = "close")
    @Scope("singleton")
    @Primary
    public BasicDataSource getDatasource() {
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName(driverClassName);
        dataSource.setPassword(password);
        dataSource.setUrl(url);
        dataSource.setInitialSize(Integer.valueOf(Objects.requireNonNull(initialSize)));
        dataSource.setUsername(username);
        dataSource.setMaxActive(Integer.valueOf(Objects.requireNonNull(maxActive)));
        return dataSource;
    }
}
