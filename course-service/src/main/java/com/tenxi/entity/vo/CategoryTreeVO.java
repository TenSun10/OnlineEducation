package com.tenxi.entity.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.tenxi.entity.po.Category;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CategoryTreeVO {
    // 核心字段
    private Long id;          // 分类ID（对应数据库主键）
    private String label;      // 分类名称
    private Integer level;    // 分类层级（1/2/3级）
    private Integer orderNum; // 排序号
    private List<CategoryTreeVO> children = new ArrayList<>(); // 子分类列表

    private Long parentId;
    //图标的url,目前没有
    private String icon;

    public static CategoryTreeVO convertToCategoryTreeVO(Category category) {
        CategoryTreeVO categoryTreeVO = new CategoryTreeVO();
        categoryTreeVO.setId(category.getId());
        categoryTreeVO.setLabel(categoryTreeVO.getLabel());
        categoryTreeVO.setLevel(category.getLevel());
        categoryTreeVO.setOrderNum(category.getOrderNum());
        categoryTreeVO.setParentId(category.getParentId());
        return categoryTreeVO;
    }

    // 添加Jackson反序列化需要的默认构造器
    public CategoryTreeVO() {
        this.children = new ArrayList<>(); // 防止null
    }
}