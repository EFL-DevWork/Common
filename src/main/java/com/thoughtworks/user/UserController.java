package com.thoughtworks.user;

import com.thoughtworks.errorcodes.InternalErrorCodes;
import com.thoughtworks.exceptions.ResourceConflictException;
import com.thoughtworks.exceptions.ResourceNotFoundException;
import com.thoughtworks.user.api.UserDetailsResp;
import com.thoughtworks.user.model.User;
import com.thoughtworks.user.api.UserRequest;
import com.thoughtworks.user.api.UserResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static com.thoughtworks.user.api.UserResponse.ADDED;
import static com.thoughtworks.user.api.UserResponse.UPDATED;

@RestController
public class UserController {

    UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/user")
    @ResponseStatus(HttpStatus.OK)
    public List<UserDetailsResp> getUsers() {
        List<User> users = userService.getUsers();
        List<UserDetailsResp> userDetailsResp = users.stream().map(UserDetailsResp::new).collect(Collectors.toList());
        return userDetailsResp;
    }

    @PostMapping("/user")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse addUser(@RequestBody UserRequest userReq) throws ResourceConflictException {
        User user = new User(userReq);
        User u = userService.addUser(user);
        return new UserResponse(String.format("%s %s", u.getFirstName(), u.getLastName()), ADDED);
    }

    @PutMapping("/user")
    @ResponseStatus(HttpStatus.OK)
    public UserResponse updateUser(@RequestBody UserRequest userReq) throws ResourceNotFoundException {
        boolean isValid = userService.validateUser(userReq.userId, userReq.password);
        if (!isValid) {
            throw new ResourceNotFoundException(InternalErrorCodes.USER_NOT_FOUND, InternalErrorCodes.USER_NOT_FOUND.getDescription());
        }
        User user = new User(userReq);
        User u = userService.updateUser(user);
        return new UserResponse(String.format("%s %s", u.getFirstName(), u.getLastName()), UPDATED);
    }
}
