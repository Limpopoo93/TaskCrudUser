package by.dao;

import by.dto.User;

import java.util.List;

public interface UserRepository <T, K> {

    List<T> findAll();

    T save(T entity);

    T update(T entity);

    void delete(K id);

    User findById(K id);

    User findByLogin (String login);
}
