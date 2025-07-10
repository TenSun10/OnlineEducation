package com.tenxi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tenxi.entity.po.Category;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CategoryMapper extends BaseMapper<Category> {
    // CategoryMapper.java
    @Select("WITH RECURSIVE subcategories AS (" +
            "SELECT id FROM category WHERE id = #{categoryId} " +
            "UNION ALL " +
            "SELECT c.id FROM category c " +
            "INNER JOIN subcategories s ON c.parent_id = s.id" +
            ") SELECT id FROM subcategories")
    List<Long> getAllSubCategoryIds(@Param("categoryId") Long categoryId);
}
