package com.manzhushaka.system.controller;

import com.manzhushaka.common.model.ApiResponse;
import com.manzhushaka.system.dto.user.UserForm;
import com.manzhushaka.system.dto.user.UserQuery;
import com.manzhushaka.system.service.UserService;
import com.manzhushaka.system.vo.PageResult;
import com.manzhushaka.system.vo.user.UserVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/system/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ApiResponse<PageResult<UserVO>> page(UserQuery query) {
        return ApiResponse.success(userService.page(query));
    }

    @GetMapping("/{id}")
    public ApiResponse<UserVO> getById(@PathVariable Long id) {
        return ApiResponse.success(userService.getById(id));
    }

    @PostMapping
    public ApiResponse<Long> create(@Valid @RequestBody UserForm form) {
        return ApiResponse.success(userService.create(form));
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @Valid @RequestBody UserForm form) {
        userService.update(id, form);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return ApiResponse.success(null);
    }
}
