package com.manzhushaka.system.controller;

import com.manzhushaka.common.model.ApiResponse;
import com.manzhushaka.system.dto.menu.MenuForm;
import com.manzhushaka.system.dto.menu.MenuQuery;
import com.manzhushaka.system.service.MenuService;
import com.manzhushaka.system.vo.LabelValueOption;
import com.manzhushaka.system.vo.MenuTreeNode;
import com.manzhushaka.system.vo.menu.MenuRouteVO;
import com.manzhushaka.system.vo.menu.MenuVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/system/menus")
public class MenuController {

    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    @GetMapping
    public ApiResponse<List<MenuVO>> list(MenuQuery query) {
        return ApiResponse.success(menuService.list(query));
    }

    @GetMapping("/tree")
    public ApiResponse<List<MenuTreeNode>> tree(MenuQuery query) {
        return ApiResponse.success(menuService.tree(query));
    }

    @GetMapping("/routes")
    public ApiResponse<List<MenuRouteVO>> routes(@RequestParam Long userId) {
        return ApiResponse.success(menuService.routesByUserId(userId));
    }

    @GetMapping("/options")
    public ApiResponse<List<LabelValueOption>> options() {
        return ApiResponse.success(menuService.options());
    }

    @GetMapping("/{id}")
    public ApiResponse<MenuVO> getById(@PathVariable Long id) {
        return ApiResponse.success(menuService.getById(id));
    }

    @PostMapping
    public ApiResponse<Long> create(@Valid @RequestBody MenuForm form) {
        return ApiResponse.success(menuService.create(form));
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @Valid @RequestBody MenuForm form) {
        menuService.update(id, form);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        menuService.delete(id);
        return ApiResponse.success(null);
    }
}
