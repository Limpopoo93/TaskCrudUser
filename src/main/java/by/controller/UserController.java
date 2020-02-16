package by.controller;

import by.dao.UserDao;
import by.dao.UserDaoImpl;
import by.dao.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class UserController extends HttpServlet {

    @Autowired
    private UserRepository userRepository;

    public UserController() {
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        process(req, resp);
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        process(req, resp);
    }

    private void process(HttpServletRequest req, HttpServletResponse resp) {
        String queryString = req.getQueryString();
        List<String> params = Arrays.asList(queryString.split("&"));
        Long userID = 0L;
        for (String param : params) {
            String[] split = param.split("=");
            userID = Long.valueOf(split[1]);

        }
        System.out.println(userRepository.findAll());
    }
}
