package com.cjj.es.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

/**
 * 数据库配置类
 * 条件性启用 MyBatis 配置
 */
@Configuration
@ConditionalOnProperty(name = "spring.datasource.url")
public class DatabaseConfig {
    
    // 这个配置类用于条件性启用数据库相关功能
    // 如果没有配置数据源URL，相关功能不会启用
}