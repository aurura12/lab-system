package com.lab.controller;

import com.lab.dto.request.UserRequest;
import com.lab.dto.response.ApiResponse;
import com.lab.dto.response.PageResponse;
import com.lab.dto.response.UserDTO;
import com.lab.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('admin')")
    public ApiResponse<PageResponse<UserDTO>> getUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(userService.getUsers(keyword, page, size));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    public ApiResponse<UserDTO> getUserById(@PathVariable UUID id) {
        return ApiResponse.success(userService.getUserById(id));
    }

    @GetMapping("/me")
    public ApiResponse<UserDTO> getCurrentUser(
            @RequestParam UUID id) {
        return ApiResponse.success(userService.getUserById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('admin')")
    public ApiResponse<UserDTO> createUser(@Valid @RequestBody UserRequest request) {
        return ApiResponse.success("用户已创建", userService.createUser(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    public ApiResponse<UserDTO> updateUser(@PathVariable UUID id, @Valid @RequestBody UserRequest request) {
        return ApiResponse.success("用户已更新", userService.updateUser(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    public ApiResponse<Void> deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
        return ApiResponse.success("用户已停用", null);
    }
}
