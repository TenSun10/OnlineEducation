package com.tenxi;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tenxi.entity.po.Category;
import com.tenxi.mapper.CategoryMapper;
import com.tenxi.service.CategoryService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class CourceServiceApplicationTests {

    @Resource
    private CategoryMapper categoryMapper;

    @Test
    void contextLoads() {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Category::getOrderNum);
        /*wrapper.eq(Category::getStatus, 1);*/
        List<Category> allCategories = categoryMapper.selectList(wrapper);
        allCategories.forEach(a -> System.out.println(a.toString()));
    }

}
