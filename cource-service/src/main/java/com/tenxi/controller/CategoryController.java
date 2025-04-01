package com.tenxi.controller;

import com.tenxi.utils.RestBean;
import com.tenxi.entity.vo.CategoryTreeVO;
import com.tenxi.handler.ControllerHandler;
import com.tenxi.service.CategoryService;
import jakarta.annotation.Resource;
import lombok.extern.java.Log;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.tenxi.entity.dto.CategoryAddDTO;

import java.util.List;

@Log
@RestController
@RequestMapping("/category")
public class CategoryController {
    @Resource
    private CategoryService categoryService;
    @Resource
    private ControllerHandler controllerHandler;

    /**
     * 实现新增逻辑
     * @param dto
     * @return
     */
    @PostMapping
    @PreAuthorize("hasRole('ROLE_admin')")
    public RestBean<String> addCategory(@RequestBody CategoryAddDTO dto) {
        return controllerHandler.messageHandler(() ->
                categoryService.addCategory(dto));
    }

    @GetMapping("/tree")
    public RestBean<List<CategoryTreeVO>> getCategoryTree() {
        return categoryService.buildCategoryTree();
    }

    @GetMapping("/status/{id}")
    public RestBean<String> changeStatus(@PathVariable Long id) {
        return controllerHandler.messageHandler(() ->
                categoryService.changeStatus(id));
    }
}
