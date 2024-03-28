package com.api19_4.api19_4.UserSystemSQL;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {


    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public AuthService(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public boolean authenticateSA(String username, String password) {
        try {
            // Ví dụ sử dụng JdbcTemplate để kiểm tra xác thực tài khoản SA
            String sql = "SELECT 1 FROM sys.server_principals WHERE name = ? AND type_desc = 'SQL_LOGIN'";
            jdbcTemplate.queryForObject(sql, Integer.class, username);

            // Nếu không có exception, tức là tài khoản SA hợp lệ
            return true;
        } catch (EmptyResultDataAccessException e) {
            // Nếu có exception, tức là tài khoản SA không tồn tại hoặc không hợp lệ
            return false;
        }
    }

    private Map<String, Long> lastSuccessfulLogins = new HashMap<>();

    public void setLastSuccessfulLogin(String username, long time) {
        lastSuccessfulLogins.put(username, time);
    }

    public long getLastSuccessfulLogin(String username) {
        return lastSuccessfulLogins.getOrDefault(username, 0L);
    }

}
