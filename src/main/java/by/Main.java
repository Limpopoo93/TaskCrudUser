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
