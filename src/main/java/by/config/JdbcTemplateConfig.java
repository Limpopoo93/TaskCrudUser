package by.config;

import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

@Configuration
@ComponentScan("by")
public class JdbcTemplateConfig {
    @Autowired
    @Qualifier("dataSource")
    private BasicDataSource dataSource;

    @Bean("jdbcTemplate")
    public JdbcTemplate getJdbcTemplate() { return new JdbcTemplate(dataSource); }

    @Bean("namedJdbcTemplate")
    public NamedParameterJdbcTemplate getNamedJdbcTemplate() { return new NamedParameterJdbcTemplate(dataSource); }

    @Bean
    public DataSourceTransactionManager getTransactionManager() { return new DataSourceTransactionManager(dataSource); }

}
