package com.cjj.es.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Elasticsearch测试服务
 * 提供ES连接检测等测试功能
 */
@Service
public class ElasticsearchTestService {
    
    @Autowired
    private ElasticsearchClient elasticsearchClient;
    
    /**
     * 检查ES连接是否正常
     * @return 连接状态
     */
    public boolean isConnected() {
        try {
            // 通过ping检查ES连接状态
            return elasticsearchClient.ping().value();
        } catch (Exception e) {
            System.err.println("ES连接检查失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 获取ES集群信息
     * @return 集群信息字符串
     */
    public String getClusterInfo() {
        try {
            return elasticsearchClient.info().clusterName();
        } catch (Exception e) {
            System.err.println("获取ES集群信息失败: " + e.getMessage());
            return "获取失败";
        }
    }
}