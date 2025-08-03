package com.cjj.es.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Dubbo服务接口模拟实现
 * 负责接收外部调用并触发数据同步流程
 * 实现Dubbo->Kafka->ES数据流转的Dubbo入口
 * 遵循接口隔离原则：提供简洁的外部调用接口
 */
@Service
public class DubboSyncService {

    @Autowired
    private ESService esService;

    /**
     * Dubbo接口：全量同步数据到ES
     * 外部系统通过此接口触发数据同步
     * 内部流程：查询数据库 -> 发送Kafka -> 写入ES
     * @return 同步结果
     */
    public Map<String, Object> syncAllDataToES() {
        System.out.println("收到Dubbo调用请求：开始全量同步数据到ES");
        
        try {
            // 调用ES服务执行同步
            Map<String, Object> result = esService.fullSyncToES();
            
            // 记录调用结果
            boolean success = (Boolean) result.get("success");
            System.out.println("Dubbo调用完成，同步结果: " + (success ? "成功" : "失败"));
            
            return result;
            
        } catch (Exception e) {
            System.err.println("Dubbo调用过程中发生错误: " + e.getMessage());
            throw new RuntimeException("数据同步失败", e);
        }
    }

    /**
     * Dubbo接口：同步单个文档到ES
     * @param aid 文章ID
     * @return 同步结果
     */
    public boolean syncSingleDocToES(Long aid) {
        System.out.println("收到Dubbo调用请求：同步单个文档到ES，ID: " + aid);
        
        try {
            boolean result = esService.saveDocument(aid);
            System.out.println("Dubbo单文档同步完成，结果: " + (result ? "成功" : "失败"));
            return result;
            
        } catch (Exception e) {
            System.err.println("Dubbo单文档同步过程中发生错误: " + e.getMessage());
            return false;
        }
    }
}