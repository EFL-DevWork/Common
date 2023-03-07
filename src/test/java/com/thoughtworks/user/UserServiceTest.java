package com.thoughtworks.user;

import com.thoughtworks.exceptions.ResourceConflictException;
import com.thoughtworks.exceptions.ResourceNotFoundException;
import com.thoughtworks.user.api.UserRequest;
import com.thoughtworks.user.model.User;
import io.opentelemetry.api.GlobalOpenTelemetry;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserServiceTest {

    @Autowired
    UserService userService;

    @Autowired
    UserRepository userRepository;


    @BeforeEach
    void tearDown() {
        userRepository.deleteAll();
        GlobalOpenTelemetry.resetForTest();
    }

    @BeforeAll
    static void beforeAll() {
        GlobalOpenTelemetry.resetForTest();
    }

    @Test
    void addUserDetails() throws ResourceConflictException {
        UserRequest userReq = new UserRequest("bruce101", "burce", "wayne", "01-01-1997", "ABR32IQ3", "password");
        User user = new User(userReq);
        User savedUser = userService.addUser(user);

        user.setId(savedUser.getId());
        assertEquals(user, savedUser);
    }

    @Test
    void cannotAddUserWhenUserAlreadyExist() throws ResourceConflictException {
        UserRequest userReq = new UserRequest("bruce101", "burce", "wayne", "01-01-1997", "ABR32IQ3", "password");
        User user = new User(userReq);
        userService.addUser(user);
        ResourceConflictException exception =
                assertThrows(ResourceConflictException.class, () -> userService.addUser(user));

        assertEquals("User already added", exception.getErrorMessage());
    }

    @Test
    void getUserDetails() throws ResourceConflictException {
        List<User> expectedUser = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            UserRequest userReq = new UserRequest(String.format("bruce10%x", i), "burce", "wayne", "01-01-1997", "ABR32IQ3", "password");
            User user = new User(userReq);
            expectedUser.add(new User(userReq));
            userService.addUser(user);
        }

        List<User> users = userService.getUsers();

        assertEquals(users.size(), expectedUser.size());
    }

    @Test
    void validateUserReturnsTrue() throws ResourceConflictException {
        UserRequest userReq = new UserRequest("bruce101", "burce", "wayne", "01-01-1997", "ABR32IQ3", "password");
        User user = new User(userReq);
        userService.addUser(user);

        boolean isValid = userService.validateUser(userReq.userId, userReq.password);

        assertTrue(isValid);
    }

    @Test
    void validateUserInvalidUserID() throws ResourceConflictException {
        UserRequest userRequest = new UserRequest("bruce101", "burce", "wayne", "01-01-1997", "ABR32IQ3", "password");
        User user = new User(userRequest);
        userService.addUser(user);
        userRequest.userId = "bruce102";

        boolean isValid = userService.validateUser(userRequest.userId, userRequest.password);

        assertFalse(isValid);
    }

    @Test
    void validateUserInvalidPassword() throws ResourceNotFoundException, ResourceConflictException {
        UserRequest userRequest = new UserRequest("bruce101", "burce", "wayne", "01-01-1997", "ABR32IQ3", "password");
        User user = new User(userRequest);
        userService.addUser(user);

        userRequest.password = "pass1234";

        boolean isValid = userService.validateUser(userRequest.userId, userRequest.password);

        assertFalse(isValid);
    }

    @Test
    void updateUser() throws ResourceConflictException, ResourceNotFoundException {
        UserRequest userRequest = new UserRequest("bruce101", "burce", "wayne", "01-01-1997", "ABR32IQ3", "password");
        User u = new User(userRequest);
        User user = userService.addUser(u);

        User updatedUser = userService.updateUser(u);

        assertEquals(user, updatedUser);
    }
}
