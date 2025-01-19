package com.example.user.service;

import com.example.common.dto.UserDto;
import com.example.user.dto.CreateUserRequest;
import com.example.user.dto.UpdateUserRequest;

import java.util.List;

public interface UserService {
    UserDto createUser(CreateUserRequest request);
    UserDto getUser(Long id);
    UserDto updateUser(Long id, UpdateUserRequest request);
    void deleteUser(Long id);
    List<UserDto> getAllUsers();
} 