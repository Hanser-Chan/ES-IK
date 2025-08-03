package com.cjj.es.consumer;

import com.cjj.es.entity.Article;
import com.cjj.es.service.ESService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Kafka消息消费者
 * 负责处理ES同步相关的Kafka消息
 * 实现Dubbo->Kafka->ES数据流转的Kafka处理环节
 * 遵循单一职责原则：专注于消息消费和ES写入
 */
@Component
public class ESKafkaConsumer {

    @Autowired
    private ESService esService;

    /**
     * 监听ES同步主题消息
     * 接收到消息后调用ES服务进行批量写入
     * @param message Kafka消息内容
     */
    @KafkaListener(topics = "es_sync_topic", groupId = "es-sync-group")
    public void handleESSyncMessage(Map<String, Object> message) {
        try {
            System.out.println("收到Kafka消息，开始处理ES同步...");
            
            // 解析消息内容
            String operation = (String) message.get("operation");
            String index = (String) message.get("index");
            List<Article> articles = (List<Article>) message.get("data");
            Long timestamp = (Long) message.get("timestamp");
            
            System.out.println("消息详情 - 操作: " + operation + ", 索引: " + index + 
                             ", 数据量: " + (articles != null ? articles.size() : 0) + 
                             ", 时间戳: " + timestamp);
            
            // 执行批量写入ES操作
            if ("bulk_index".equals(operation) && articles != null) {
                int successCount = esService.processBulkIndexFromKafka(articles);
                System.out.println("Kafka消息处理完成，成功写入ES文档数量: " + successCount);
            } else {
                System.err.println("不支持的操作类型或数据为空: " + operation);
            }
            
        } catch (Exception e) {
            System.err.println("处理Kafka消息时发生错误: " + e.getMessage());
            e.printStackTrace();
            // 在实际应用中，这里应该实现重试逻辑或错误处理机制
        }
    }
}