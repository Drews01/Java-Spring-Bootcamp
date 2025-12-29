package com.example.demo.service;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User createUser(User user) {
        return userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findByDeletedFalse();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .filter(u -> u.getDeleted() == null || !u.getDeleted())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    public User updateUser(Long id, User userDetails) {
        User user = getUserById(id);
        user.setUsername(userDetails.getUsername());
        user.setEmail(userDetails.getEmail());
        user.setIsActive(userDetails.getIsActive());
        if (userDetails.getRoles() != null) {
            user.setRoles(userDetails.getRoles());
        }
        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        User user = getUserById(id);
        user.setDeleted(true);
        user.setIsActive(false);
        userRepository.save(user);
    }
}
