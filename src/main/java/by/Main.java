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
    @Service
public class KafkaConsumerService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final PostgresTableService tableService;

    public KafkaConsumerService(PostgresTableService tableService) {
        this.tableService = tableService;
    }

    @KafkaListener(topics = "your-kafka-topic", groupId = "your-group")
    public void consume(String message) {
        try {
            JsonNode jsonNode = objectMapper.readTree(message);
            String tableName = jsonNode.get("table").asText();
            JsonNode afterNode = jsonNode.get("after");

            Map<String, Object> data = new HashMap<>();
            afterNode.fields().forEachRemaining(field -> {
                data.put(field.getKey(), parseJsonNode(field.getValue()));
            });

            tableService.processMessage(tableName, data);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Object parseJsonNode(JsonNode node) {
        if (node.isInt()) return node.intValue();
        if (node.isDouble()) return node.doubleValue();
        if (node.isBoolean()) return node.booleanValue();
        return node.asText();
    }
}
--------------------------------------------
    @Service
public class PostgresTableService {

    private final JdbcTemplate jdbcTemplate;
    private final SchemaLockService lockService;

    public PostgresTableService(JdbcTemplate jdbcTemplate, SchemaLockService lockService) {
        this.jdbcTemplate = jdbcTemplate;
        this.lockService = lockService;
    }

    @Retryable(
        value = { TimeoutException.class },
        maxAttempts = Integer.MAX_VALUE,
        backoff = @Backoff(delay = 5000)
    )
    public void processMessage(String tableName, Map<String, Object> data) {
        lockService.lockTable(tableName);

        try {
            long startTime = System.currentTimeMillis();

            if (!tableExists(tableName)) {
                createTable(tableName, data);
            } else {
                addMissingColumns(tableName, data);
            }

            insertData(tableName, data);

            long duration = System.currentTimeMillis() - startTime;
            if (duration > 15000) {
                throw new TimeoutException("DDL operation exceeded 15s");
            }

        } finally {
            lockService.unlockTable(tableName);
        }
    }

    private boolean tableExists(String tableName) {
        String sql = "SELECT EXISTS (SELECT FROM information_schema.tables WHERE table_name = ?)";
        return jdbcTemplate.queryForObject(sql, Boolean.class, tableName);
    }

    private void createTable(String tableName, Map<String, Object> data) {
        StringBuilder sql = new StringBuilder("CREATE TABLE " + tableName + " (");

        data.forEach((key, value) -> {
            sql.append(key).append(" ").append(getSQLType(value)).append(", ");
        });

        sql.setLength(sql.length() - 2); // remove last comma
        sql.append(")");

        jdbcTemplate.execute(sql.toString());
    }

    private void addMissingColumns(String tableName, Map<String, Object> data) {
        Set<String> existingColumns = getExistingColumns(tableName);

        for (Map.Entry<String, Object> entry : data.entrySet()) {
            if (!existingColumns.contains(entry.getKey())) {
                String sql = String.format("ALTER TABLE %s ADD COLUMN %s %s", 
                        tableName, 
                        entry.getKey(), 
                        getSQLType(entry.getValue()));
                jdbcTemplate.execute(sql);
            }
        }
    }

    private Set<String> getExistingColumns(String tableName) {
        String sql = "SELECT column_name FROM information_schema.columns WHERE table_name = ?";
        return new HashSet<>(jdbcTemplate.queryForList(sql, String.class, tableName));
    }

    private void insertData(String tableName, Map<String, Object> data) {
        String columns = String.join(", ", data.keySet());
        String placeholders = data.keySet().stream().map(k -> "?").collect(Collectors.joining(", "));

        String sql = String.format("INSERT INTO %s (%s) VALUES (%s)", tableName, columns, placeholders);
        jdbcTemplate.update(sql, data.values().toArray());
    }

    private String getSQLType(Object value) {
        if (value instanceof Integer) return "INT";
        if (value instanceof Double || value instanceof Float) return "DOUBLE PRECISION";
        if (value instanceof Boolean) return "BOOLEAN";
        return "TEXT";
    }
}
--------------------------------------------------------------
    @Service
public class SchemaLockService {

    private final ConcurrentHashMap<String, ReentrantLock> tableLocks = new ConcurrentHashMap<>();

    public void lockTable(String tableName) {
        ReentrantLock lock = tableLocks.computeIfAbsent(tableName, k -> new ReentrantLock(true));

        try {
            while (!lock.tryLock(1, TimeUnit.SECONDS)) {
                // wait until lock is available
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void unlockTable(String tableName) {
        ReentrantLock lock = tableLocks.get(tableName);
        if (lock != null && lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }
}
