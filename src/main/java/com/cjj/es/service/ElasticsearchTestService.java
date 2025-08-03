package com.cjj.es.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch.cluster.HealthResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class ElasticsearchTestService implements CommandLineRunner {

    @Autowired
    private ElasticsearchClient elasticsearchClient;

    @Override
    public void run(String... args) throws Exception {
        testConnection();
    }

    /**
     * 测试 Elasticsearch 连接
     */
    public void testConnection() {
        try {
            System.out.println("==== 开始测试 Elasticsearch 连接 ====");
            
            // 获取集群信息
            var infoResponse = elasticsearchClient.info();
            System.out.println("连接成功！");
            System.out.println("集群名称: " + infoResponse.clusterName());
            System.out.println("集群UUID: " + infoResponse.clusterUuid());
            System.out.println("Elasticsearch版本: " + infoResponse.version().number());
            System.out.println("Lucene版本: " + infoResponse.version().luceneVersion());

            
            System.out.println("==== Elasticsearch 连接测试完成 ====");
            
        } catch (ElasticsearchException e) {
            System.err.println("Elasticsearch 错误: " + e.getMessage());
            e.printStackTrace();
            
        } catch (IOException e) {
            System.err.println("IO 错误: " + e.getMessage());
            e.printStackTrace();
            
        } catch (Exception e) {
            System.err.println("未知错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 手动测试连接方法
     */
    public boolean isConnected() {
        try {
            elasticsearchClient.info();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}