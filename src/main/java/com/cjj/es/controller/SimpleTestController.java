package com.cjj.es.controller;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.cjj.es.entity.Article;
import com.cjj.es.mapper.ArticleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Elasticsearch 测试控制器
 * 提供简单的测试接口
 */
@RestController
@RequestMapping("/api/test")
public class SimpleTestController {

    @Autowired
    private ElasticsearchClient elasticsearchClient;

    @Autowired(required = false)
    private ArticleMapper articleMapper;

    /**
     * 测试 ES 连接
     */
    @GetMapping("/es-connection")
    public Map<String, Object> testESConnection() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            var info = elasticsearchClient.info();
            result.put("success", true);
            result.put("cluster_name", info.clusterName());
            result.put("version", info.version().number());
            return result;
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
            return result;
        }
    }

    /**
     * 测试数据库连接
     */
    @GetMapping("/db-connection")
    public Map<String, Object> testDBConnection() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            if (articleMapper == null) {
                result.put("success", false);
                result.put("message", "DocMapper 未注入，请检查 MyBatis 配置");
                return result;
            }

            int count = articleMapper.countArticles();
            result.put("success", true);
            result.put("article_count", count);
            result.put("message", "数据库连接正常");
            return result;
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
            return result;
        }
    }

    /**
     * 简单的 saveDoc 测试接口
     */
    @GetMapping("/save-doc")
    public Map<String, Object> saveDoc() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            result.put("success", true);
            result.put("message", "saveDoc 接口可用，等待完整实现");
            result.put("timestamp", System.currentTimeMillis());
            
            // 如果 docMapper 可用，尝试查询数据
            if (articleMapper != null) {
                List<Article> articles = articleMapper.selectAllArticles();
                result.put("database_articles", articles != null ? articles.size() : 0);
            }
            
            return result;
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
            return result;
        }
    }
}