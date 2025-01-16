package com.example.ordermanagement.service;

import com.example.ordermanagement.dto.CreateUserRequest;
import com.example.ordermanagement.dto.UserDto;

import java.util.List;

public interface UserService {
    UserDto createUser(CreateUserRequest request);

    UserDto getUser(Long id);

    List<UserDto> getAllUsers();

    void deleteUser(Long id);
} 