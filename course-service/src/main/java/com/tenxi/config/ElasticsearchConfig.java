package com.tenxi.config;

import com.tenxi.entity.es.CourseDocument;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.common.settings.Settings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.annotations.Setting;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
@EnableElasticsearchRepositories(basePackages = "com.tenxi.entity.es")
public class ElasticsearchConfig {

    @Value("${oe.elasticsearch.auto-create-index}")
    private boolean autoCreateIndex;

    @Value("${oe.elasticsearch.index-setting.shards}")
    private int shards;

    @Value("${oe.elasticsearch.index-setting.replicas}")
    private int replicas;


    @Bean
    @ConditionalOnBean(ElasticsearchOperations.class)
    public CommandLineRunner commandLineRunner(ElasticsearchOperations elasticsearchOperations) {
        return args -> {
            if (autoCreateIndex) {
                initializeCourseIndex(elasticsearchOperations);
            }
        };
    }

    //索引初始化
    private void initializeCourseIndex(ElasticsearchOperations elasticsearchOperations) {
        try {
            //1.获取索引操作接口
            IndexOperations indexOps = elasticsearchOperations.indexOps(CourseDocument.class);
            //2.检查索引是否已经存在
            if (!indexOps.exists()) {

                //3.自定义设置
                Map<String, Object> settings = new HashMap<>();
                settings.put("number_of_shards", shards);
                settings.put("number_of_replicas", replicas);

                //4.创建索引
                indexOps.create(settings);

                //5.创建映射
                indexOps.putMapping();

                log.info("Course索引创建成功 - Shards: {}, Replicas: {}", shards, replicas);
            } else {
                log.info("Course索引已存在，跳过创建");
            }

        }catch (Exception e) {
            log.error("Course索引初始化失败", e);
        }
    }
}
