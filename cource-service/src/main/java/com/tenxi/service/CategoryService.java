package com.tenxi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tenxi.utils.RestBean;
import com.tenxi.entity.dto.CategoryAddDTO;
import com.tenxi.entity.po.Category;
import com.tenxi.entity.vo.CategoryTreeVO;


import java.util.List;

public interface CategoryService extends IService<Category> {

    String addCategory(CategoryAddDTO dto);

    RestBean<List<CategoryTreeVO>> buildCategoryTree();

    String changeStatus(Long id);
}
