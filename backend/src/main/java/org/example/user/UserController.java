package org.example.user;

import org.example.Resp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "http://127.0.0.1:8000", allowCredentials = "true")
public class UserController {

    @Autowired
    private UserRepo userRepo;

    @PersistenceContext
    private EntityManager em;

    @PostMapping("/login")
    public Resp login(@RequestParam String account, @RequestParam String password, HttpServletResponse resp) {

        Resp res = new Resp();

        User user = userRepo.findByAccount(account);
        if (user.getPassword().equals(password)) {
            Cookie cookie = new Cookie("user", user.getId().toString());
            cookie.setMaxAge(60*60);
            resp.addCookie(cookie);
            res.setMsg("login success");
            res.setStatus("success");
            return res;
        } else {
            res.setMsg("password error");
            res.setStatus("error");
            return res;
        }
    }

    @PostMapping("/users")
    public Resp addUser(@RequestBody User data) {

        Resp res = new Resp();

        String name = data.getName();
        String password = data.getPassword();
        String account = data.getAccount();

        User exists = userRepo.findByAccount(account);
        if (exists == null) {
            User user = new User();
            user.setAccount(account);
            user.setName(name);
            user.setPassword(password);
            userRepo.save(user);

            res.setStatus("success");
            res.setMsg("添加用户成功");
            res.setData(user);
        } else {
            res.setStatus("error");
            res.setMsg("用户已存在");
        }

        return res;
    }

    @DeleteMapping("/users/{id}")
    public User deleteUser(@PathVariable Integer id) {
        User user = new User();
        Optional<User> userQuery = userRepo.findById(id);
        if (userQuery.isPresent()) {
            user = userQuery.get();
            userRepo.delete(user);
        }

        return  user;
    }

    @PutMapping("/users/{id}")
    public User updateUser(@PathVariable Integer id, @RequestBody User data) {

        User user = new User();

        String account = data.getAccount();
        String name = data.getName();
        String password = data.getPassword();

        Optional<User> userQuery = userRepo.findById(id);

        if (userQuery.isPresent()) {
            user = userQuery.get();

            if (account != null && !account.equals("")) {
                user.setAccount(account);
            }

            if (name != null && !name.equals("")) {
                user.setName(name);
            }

            if (password != null && !password.equals("")) {
                user.setAccount(password);
            }

            userRepo.save(user);
        }

        return  user;
    }

    @GetMapping("/users")
    public List<User> search(HttpServletRequest req) {

        List<User> users = new ArrayList<>();
        

        String sql = "select * from User where 1 = 1 ";

        if (req.getParameter("account") != null && !req.getParameter("account").equals("")) {
            sql += "and account = '" + req.getParameter("account") + "'";
        }

        if (req.getParameter("name") != null && !req.getParameter("name").equals("")) {
            sql += "and name like '%" + req.getParameter("name") + "%'";
        }

        System.out.println(sql);

        Query q = em.createNativeQuery(sql);
        users = q.getResultList();

        return users;
    }

}
