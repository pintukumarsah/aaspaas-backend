package com.aaspaas.aaspaas_backend.user.serviceimpl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.aaspaas.aaspaas_backend.common.exception.ResourceNotFoundException;
import com.aaspaas.aaspaas_backend.user.dto.UserResponse;
import com.aaspaas.aaspaas_backend.user.entity.User;
import com.aaspaas.aaspaas_backend.user.mapper.UserMapper;
import com.aaspaas.aaspaas_backend.user.repository.UserRepository;
import com.aaspaas.aaspaas_backend.user.service.UserService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public UserResponse getUserById(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
        new ResourceNotFoundException(
                "User not found with id: " + id
        )
);

        return userMapper.toResponse(user);
    }

    @Override
    public List<UserResponse> getAllUsers() {

        return userRepository.findAll()
                .stream()
                .map(userMapper::toResponse)
                .toList();
    }
}