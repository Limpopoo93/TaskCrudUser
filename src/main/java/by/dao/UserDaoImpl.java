package by.dao;

import by.dto.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository("UserRepositoryImpl")
@Transactional
public class UserDaoImpl implements UserDao {
    public static final String USER_ID = "id";
    public static final String USER_LOGIN = "login";
    public static final String USER_PASSWORD = "password";
    public static final String USER_ROLE = "role";

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    public UserDaoImpl(JdbcTemplate jdbcTemplate,
                              NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    private User getUserRowMapper(ResultSet set, int i) throws SQLException {
        User user = new User();
        user.setId(set.getLong(USER_ID));
        user.setLogin(set.getString(USER_LOGIN));
        user.setPassword(set.getString(USER_PASSWORD));
        user.setRole(set.getString(USER_ROLE));
        return user;
    }


    @Override
    public List<User> findAll() {
        final String findAllQuery = "select * from users";
        return namedParameterJdbcTemplate.query(findAllQuery, this::getUserRowMapper);
    }

    @Override
    public User save(User user) {
        final String createQueryForUsers = "INSERT INTO users (login, password, role) VALUES (:login, :password, :role);";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        MapSqlParameterSource paramsForUsers = new MapSqlParameterSource();
        paramsForUsers.addValue("login", user.getLogin());
        paramsForUsers.addValue("password", user.getPassword());
        paramsForUsers.addValue("role", user.getPassword());
        namedParameterJdbcTemplate.update(createQueryForUsers, paramsForUsers, keyHolder);

        return findById(findByLogin(user.getLogin()).getId());
    }


    @Override
    public void delete(Long id) {

        final String delete = "DELETE FROM users where id = :id";

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("id", id);

        namedParameterJdbcTemplate.update(delete, params);

    }

    @Override
    public User findById(Long id) {

        final String findById = "select * from users where id = :id";

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("id", id);

        return namedParameterJdbcTemplate.queryForObject(findById, params, this::getUserRowMapper);
    }

    @Override
    public User findByLogin(String login) {

        final String findByLogin = "select * from users where login = :login";

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("login", login);

        return namedParameterJdbcTemplate.queryForObject(findByLogin, params, this::getUserRowMapper);
    }

    @Override
    public User update(User user) {
        final String createQuery = "UPDATE users set login = :login, password = :password, role = :role where id = :id";

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("login", user.getLogin());
        params.addValue("password", user.getPassword());
        params.addValue("role", user.getPassword());
        params.addValue("id", user.getId());
        namedParameterJdbcTemplate.update(createQuery, params);
        return findById(user.getId());
    }
}
