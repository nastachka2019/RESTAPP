package com.leverX.blog.service;

import com.leverX.blog.model.User;
import com.leverX.blog.model.RegistrationRequest;

public interface UserService {

    User findByEmail(String email);

    User findByLogin(String login);

    User save(User user);

    User createUser(RegistrationRequest registrationRequest);

    User findByLoginAndPassword(String login, String password);
}
