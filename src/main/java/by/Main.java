package by;

import by.config.AppConfig;
import by.dao.UserDao;
import by.dto.User;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Main {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
        UserDao userDao = (UserDao) context.getBean("UserRepositoryImpl");

        System.out.println("Лист юзеров");
        for (User user : userDao.findAll()) {
            System.out.println(user.toString());
        }

        System.out.println("Поиск по ID");
        System.out.println(userDao.findById(1L));

        System.out.println("Добавление юзера");
        User user = new User();
        user.setLogin("user11");
        user.setPassword("1234");
        user.setRole("user");
        System.out.println("Юзер сохранен - возврат");
        User user1 = userDao.save(user);
        System.out.println(user1);

        System.out.println("Удаляю юзера");
        userDao.delete(1L);


        System.out.println("Поиск юзера по Login");
        System.out.println(userDao.findByLogin("user1"));

        System.out.println("Изменение юзера");
        user1.setLogin("userUpdate");
        user1.setPassword("123456");
        user1.setRole("admin");
        System.out.println(userDao.update(user1));

    }

}
----------------------------------------
 @SpringBootApplication
@EnableRetry
@EnableAsync
public class KafkaPostgresApplication {
    public static void main(String[] args) {
        SpringApplication.run(KafkaPostgresApplication.class, args);
    }
}
----------------------------------------
    @Data
@NoArgsConstructor
@AllArgsConstructor
public class KafkaMessage {
    private String table;
    private Map<String, Object> after;
}
----------------------------------------------
    @Service
@Slf4j
public class TableService {
    
    @Value("${app.table-lock-timeout:15}")
    private int tableLockTimeout;
    
    @Value("${app.max-retry-attempts:3}")
    private int maxRetryAttempts;
    
    @Value("${app.retry-delay:1000}")
    private int retryDelay;
    
    private final Map<String, ReentrantLock> tableLocks = new ConcurrentHashMap<>();
    
    @Retryable(
        value = {SQLException.class, SQLTimeoutException.class},
        maxAttemptsExpression = "${app.max-retry-attempts:3}",
        backoff = @Backoff(delayExpression = "${app.retry-delay:1000}")
    )
    public void processMessage(KafkaMessage message, DataSource dataSource) {
        String tableName = message.getTable();
        Map<String, Object> data = message.getAfter();
        
        // Получаем или создаем блокировку для таблицы
        ReentrantLock tableLock = tableLocks.computeIfAbsent(tableName, k -> new ReentrantLock(true));
        
        try {
            // Пытаемся получить блокировку с таймаутом
            if (tableLock.tryLock(tableLockTimeout, TimeUnit.SECONDS)) {
                try (Connection connection = dataSource.getConnection()) {
                    connection.setAutoCommit(false);
                    
                    try {
                        // Проверяем существование таблицы
                        if (!tableExists(connection, tableName)) {
                            createTable(connection, tableName, data);
                        } else {
                            // Проверяем и добавляем отсутствующие колонки
                            addMissingColumns(connection, tableName, data);
                        }
                        
                        // Вставляем данные
                        insertData(connection, tableName, data);
                        
                        connection.commit();
                        log.info("Successfully processed message for table: {}", tableName);
                        
                    } catch (SQLException e) {
                        connection.rollback();
                        throw e;
                    }
                }
            } else {
                throw new SQLTimeoutException("Timeout while waiting for table lock: " + tableName);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Thread interrupted while waiting for table lock", e);
        } finally {
            if (tableLock.isHeldByCurrentThread()) {
                tableLock.unlock();
            }
        }
    }
    
    private boolean tableExists(Connection connection, String tableName) throws SQLException {
        String sql = "SELECT EXISTS (SELECT FROM information_schema.tables " +
                    "WHERE table_schema = 'public' AND table_name = ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, tableName);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getBoolean(1);
            }
        }
    }
    
    private void createTable(Connection connection, String tableName, Map<String, Object> data) 
            throws SQLException {
        
        StringBuilder sql = new StringBuilder("CREATE TABLE ")
                .append(tableName)
                .append(" (id SERIAL PRIMARY KEY");
        
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            sql.append(", ").append(entry.getKey()).append(" ").append(getSqlType(entry.getValue()));
        }
        
        sql.append(")");
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql.toString());
            log.info("Created table: {}", tableName);
        }
    }
    
    private void addMissingColumns(Connection connection, String tableName, Map<String, Object> data) 
            throws SQLException {
        
        Set<String> existingColumns = getExistingColumns(connection, tableName);
        
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            String columnName = entry.getKey();
            if (!existingColumns.contains(columnName)) {
                addColumn(connection, tableName, columnName, entry.getValue());
            }
        }
    }
    
    private Set<String> getExistingColumns(Connection connection, String tableName) 
            throws SQLException {
        
        Set<String> columns = new HashSet<>();
        String sql = "SELECT column_name FROM information_schema.columns " +
                    "WHERE table_schema = 'public' AND table_name = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, tableName);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    columns.add(rs.getString("column_name"));
                }
            }
        }
        return columns;
    }
    
    private void addColumn(Connection connection, String tableName, String columnName, Object value) 
            throws SQLException {
        
        String sqlType = getSqlType(value);
        String sql = String.format("ALTER TABLE %s ADD COLUMN %s %s", tableName, columnName, sqlType);
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
            log.info("Added column {} to table {}", columnName, tableName);
        }
    }
    
    private void insertData(Connection connection, String tableName, Map<String, Object> data) 
            throws SQLException {
        
        StringBuilder columns = new StringBuilder();
        StringBuilder values = new StringBuilder();
        List<Object> params = new ArrayList<>();
        
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            if (columns.length() > 0) {
                columns.append(", ");
                values.append(", ");
            }
            columns.append(entry.getKey());
            values.append("?");
            params.add(entry.getValue());
        }
        
        String sql = String.format("INSERT INTO %s (%s) VALUES (%s)", 
                tableName, columns.toString(), values.toString());
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            stmt.executeUpdate();
        }
    }
    
    private String getSqlType(Object value) {
        if (value instanceof Integer || value instanceof Long) {
            return "BIGINT";
        } else if (value instanceof Double || value instanceof Float) {
            return "DOUBLE PRECISION";
        } else if (value instanceof Boolean) {
            return "BOOLEAN";
        } else if (value instanceof java.util.Date) {
            return "TIMESTAMP";
        } else {
            return "TEXT";
        }
    }
    
    @Recover
    public void recover(SQLException e, KafkaMessage message, DataSource dataSource) {
        log.error("Failed to process message for table {} after retries: {}", 
                message.getTable(), e.getMessage());
        // Здесь можно добавить логику для отправки сообщения в DLQ или логирования ошибки
    }
}
------------------------------------------------------
    @Component
@Slf4j
public class KafkaConsumerService {
    
    private final TableService tableService;
    private final DataSource dataSource;
    private final ObjectMapper objectMapper;
    
    public KafkaConsumerService(TableService tableService, DataSource dataSource, 
                               ObjectMapper objectMapper) {
        this.tableService = tableService;
        this.dataSource = dataSource;
        this.objectMapper = objectMapper;
    }
    
    @KafkaListener(topics = "${kafka.topic:postgres-updates}", groupId = "${spring.kafka.consumer.group-id}")
    @Async
    public void listen(String message) {
        try {
            log.info("Received message: {}", message);
            
            KafkaMessage kafkaMessage = objectMapper.readValue(message, KafkaMessage.class);
            
            tableService.processMessage(kafkaMessage, dataSource);
            
        } catch (JsonProcessingException e) {
            log.error("Failed to parse JSON message: {}", message, e);
        } catch (Exception e) {
            log.error("Error processing message: {}", message, e);
        }
    }
}
----------------------------------------------------------
    @Configuration
@EnableAsync
public class AsyncConfig {
    
    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("kafka-consumer-");
        executor.initialize();
        return executor;
    }
}
-----------------------------------------------------
    @Configuration
public class KafkaConfig {
    
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;
    
    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "postgres-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return new DefaultKafkaConsumerFactory<>(props);
    }
    
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setConcurrency(3);
        return factory;
    }
}
----------------------------------------------------------------------------
    @Configuration
public class JacksonConfig {
    
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper;
    }
}
---------------------------------------------------
