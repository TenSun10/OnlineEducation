package com.tenxi.listener;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tenxi.entity.po.CourseTag;
import com.tenxi.entity.po.Tag;
import com.tenxi.mapper.CourseTagMapper;
import com.tenxi.mapper.TagMapper;
import jakarta.annotation.Resource;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;

@Component
@RabbitListener(bindings = {
        @QueueBinding(
                value=@Queue(name = "tag_handler"),
                exchange = @Exchange(name = "online.edu.direct", type = ExchangeTypes.DIRECT),
                key = "tag_save_op"
        )
})
public class TagHandlerListener {

    @Resource
    private TagMapper tagMapper;
    @Resource
    private CourseTagMapper courseTagMapper;

    @RabbitHandler
    public void processTagHandle(String message) {
        // 1. 解析消息内容
        String[] parts = message.split(",");
        Long courseId = Long.parseLong(parts[0]);
        String tags = parts[1];

        // 2. 处理每个标签
        String[] tagNames = tags.split("#");
        LambdaQueryWrapper<Tag> queryWrapper = new LambdaQueryWrapper<>();
        for (int i = 1; i < tagNames.length; i++) {
            String tagName = tagNames[i];
            queryWrapper.eq(Tag::getName, tagName);
            // 2.1 查询或创建标签
            Tag tag = tagMapper.selectOne(queryWrapper); // 假设有一个方法根据标签名查询标签
            if (tag == null) {
                tag = new Tag();
                tag.setName(tagName);
                tag.setCreateTime(LocalDateTime.now());
                tagMapper.insert(tag); // 插入新标签
            }

            // 2.2 存储课程与标签的关联关系
            CourseTag courseTag = new CourseTag();
            courseTag.setCourseId(courseId);
            courseTag.setTagId(tag.getId());
            int bool = courseTagMapper.insert(courseTag);
            if(bool == 0) {
                throw new RuntimeException("tag处理失败");
            }
        }
    }
}
