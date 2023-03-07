package com.thoughtworks.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thoughtworks.CryptoConverter;
import com.thoughtworks.errorcodes.InternalErrorCodes;
import com.thoughtworks.config.CryptoConfig;
import com.thoughtworks.exceptions.ResourceConflictException;
import com.thoughtworks.filter.PrometheusFilterConfig;
import com.thoughtworks.handlers.ErrorResponse;
import com.thoughtworks.metrics.Prometheus;
import com.thoughtworks.user.api.UserDetailsResp;
import com.thoughtworks.user.api.UserRequest;
import com.thoughtworks.user.api.UserResponse;
import com.thoughtworks.user.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.thoughtworks.user.api.UserResponse.ADDED;
import static com.thoughtworks.user.api.UserResponse.UPDATED;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import({PrometheusFilterConfig.class, CryptoConfig.class})
class UserControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    UserService userService;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    Prometheus prometheus;

    @Autowired
    CryptoConverter cryptoConverter;

    @Test
       void addUserDetails() throws Exception {
        UserRequest userReq = new UserRequest("bruce101", "burce", "wayne", "01-01-1997", "ABR32IQ3", "password");
        when(userService.addUser(any())).thenReturn(new User(userReq));

        String reqData = objectMapper.writeValueAsString(userReq);
        UserResponse expectedResponse = new UserResponse(String.format("%s %s", userReq.firstName, userReq.lastName), ADDED);

        mockMvc.perform(post("/user")
                .content(reqData)
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(content().string(objectMapper.writeValueAsString(expectedResponse)));
    }

    @Test
    void cannotAddUserDetails() throws Exception {
        UserRequest userReq = new UserRequest("bruce101", "burce", "wayne", "01-01-1997", "ABR32IQ3", "password");
        when(userService.addUser(any())).thenThrow(new ResourceConflictException(InternalErrorCodes.USER_ALREADY_EXIST, InternalErrorCodes.USER_ALREADY_EXIST.getDescription()));

        String reqData = objectMapper.writeValueAsString(userReq);

        Map<String, String> expectedReason = new HashMap<>();
        expectedReason.put(InternalErrorCodes.USER_ALREADY_EXIST.toString(), "User already added");

        mockMvc.perform(post("/user")
                .content(reqData)
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(content().string(objectMapper.writeValueAsString(new ErrorResponse().message("REQUEST_CONFLICT").reasons(expectedReason))));
    }

    @Test
    void getUserDetails() throws Exception {
        List<User> userList = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            User user = new User(new UserRequest(String.format("bruce10%x", i), "burce", "wayne", "01-01-1997", "ABR32IQ3", "password"));
            userList.add(user);
        }
        List<UserDetailsResp> userDetailsResp = userList.stream().map(UserDetailsResp::new).collect(Collectors.toList());

        when(userService.getUsers()).thenReturn(userList);

        mockMvc.perform(get("/user"))
                .andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(userDetailsResp)));
    }

    @Test
    void updateUserDetails() throws Exception {
        UserRequest userReq = new UserRequest("bruce101", "burce", "wayne", "01-01-1997", "ABR32IQ3", "password");
        when(userService.updateUser(any())).thenReturn(new User(userReq));
        when(userService.validateUser(anyString(), anyString())).thenReturn(true);

        String reqData = objectMapper.writeValueAsString(userReq);
        UserResponse expectedResponse = new UserResponse(String.format("%s %s", userReq.firstName, userReq.lastName), UPDATED);

        mockMvc.perform(put("/user")
                .content(reqData)
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(expectedResponse)));
    }

    @Test
    void cannotUpdateUserInvalidUserID() throws Exception {
        UserRequest userReq = new UserRequest("bruce101", "burce", "wayne", "01-01-1997", "ABR32IQ3", "password");
        when(userService.validateUser(anyString(), anyString())).thenReturn(false);

        String reqData = objectMapper.writeValueAsString(userReq);
        Map<String, String> errors = new HashMap<>();
        errors.put(InternalErrorCodes.USER_NOT_FOUND.toString(), InternalErrorCodes.USER_NOT_FOUND.getDescription());
        ErrorResponse failureResponse = new ErrorResponse().message("MISSING_INFO").reasons(errors);

        mockMvc.perform(put("/user")
                .content(reqData)
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(content().string(objectMapper.writeValueAsString(failureResponse)));
    }

    @Test
    void cannotUpdateUserInvalidPassword() throws Exception {
        UserRequest userReq = new UserRequest("bruce101", "burce", "wayne", "01-01-1997", "ABR32IQ3", "password");
        when(userService.validateUser(anyString(), anyString())).thenReturn(false);

        String reqData = objectMapper.writeValueAsString(userReq);
        Map<String, String> errors = new HashMap<>();
        errors.put(InternalErrorCodes.USER_NOT_FOUND.toString(), InternalErrorCodes.USER_NOT_FOUND.getDescription());
        ErrorResponse failureResponse = new ErrorResponse().message("MISSING_INFO").reasons(errors);

        mockMvc.perform(put("/user")
                .content(reqData)
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf()))
                .andExpect(content().string(objectMapper.writeValueAsString(failureResponse)));
    }
}
