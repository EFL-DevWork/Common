package com.thoughtworks.user;

import com.thoughtworks.CryptoConverter;
import com.thoughtworks.errorcodes.InternalErrorCodes;
import com.thoughtworks.exceptions.ResourceConflictException;
import com.thoughtworks.exceptions.ResourceNotFoundException;
import com.thoughtworks.user.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    UserRepository userRepository;

    CryptoConverter cryptoConverter;

    @Autowired
    public UserService(UserRepository userRepository, CryptoConverter cryptoConverter) {
        this.userRepository = userRepository;
        this.cryptoConverter = cryptoConverter;
    }

    public User addUser(User user) throws ResourceConflictException {
        Optional<User> existingUser = userRepository.findByUserId(user.getUserId());
        if (existingUser.isPresent())
            throw new ResourceConflictException(InternalErrorCodes.USER_ALREADY_EXIST, InternalErrorCodes.USER_ALREADY_EXIST.getDescription());
        return userRepository.save(user);
    }

    public List<User> getUsers() {
        return userRepository.findAll();
    }

    public boolean validateUser(String userId, String password) {
        Optional<User> searchedUser = userRepository.findByUserId(userId);
        boolean isPresent = true;
        if (!searchedUser.isPresent() ||
                !cryptoConverter.getbCryptPasswordEncoder().matches(password, searchedUser.get().getPassword())) {
            isPresent = false;
        }
        return isPresent;
    }

    public User updateUser(User user) {
        Optional<User> searchedUser = userRepository.findByUserId(user.getUserId());
        user.setId(searchedUser.get().getId());
        return userRepository.save(user);
    }
}
