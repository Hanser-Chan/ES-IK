package com.cjj.es.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.Result;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.elasticsearch.core.bulk.IndexOperation;
import com.cjj.es.entity.Article;
import com.cjj.es.mapper.DocMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Elasticsearch服务层
 * 负责ES文档的增删改操作和数据流转处理
 * 遵循单一职责原则：专注于ES操作逻辑和数据处理
 * 遵循开闭原则：对扩展开放，对修改封闭
 */
@Service
public class ESService {

    @Autowired
    private ElasticsearchClient elasticsearchClient;

    @Autowired
    private DocMapper docMapper;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    // ES索引名称
    private static final String INDEX_NAME = "article_index";

    // Kafka主题名称
    private static final String KAFKA_TOPIC = "es_sync_topic";

    // HTML标签清理的正则表达式
    private static final Pattern HTML_TAG_PATTERN = Pattern.compile("<[^>]+>");

    /**
     * HTML标签清理工具方法
     * 移除content字段中的HTML标签，保证写入ES的数据纯净
     * @param htmlContent 包含HTML标签的内容
     * @return 清理后的纯文本内容
     */
    public String removeHtmlTags(String htmlContent) {
        if (htmlContent == null || htmlContent.trim().isEmpty()) {
            return "";
        }

        // 移除HTML标签
        String cleanContent = HTML_TAG_PATTERN.matcher(htmlContent).replaceAll("");

        // 清理多余的空白字符
        cleanContent = cleanContent.replaceAll("\\s+", " ").trim();

        // 解码HTML实体（如&nbsp;、&lt;等）
        cleanContent = cleanContent.replace("&nbsp;", " ")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&amp;", "&")
                .replace("&quot;", "\"");

        return cleanContent;
    }

    /**
     * 构建ES文档数据
     * 将数据库Article实体转换为ES文档格式
     * 不做复杂格式转换，直接映射三个核心字段
     * @param article 数据库文章实体
     * @return ES文档数据Map
     */
    private Map<String, Object> buildESDocument(Article article) {
        Map<String, Object> document = new HashMap<>();

        // 直接映射aid字段
        document.put("aid", article.getAid());

        // 直接映射title字段
        document.put("title", article.getTitle());

        // 清理HTML标签后映射content字段
        String cleanContent = removeHtmlTags(article.getContent());
        document.put("content", cleanContent);

        // 添加时间戳字段（使用当前时间）
        document.put("timestamp", java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yy/MM/dd HH:mm:ss")));

        return document;
    }

    /**
     * 发送数据到Kafka
     * 实现Dubbo->Kafka->ES的数据流转第二步
     * @param articles 文章数据列表
     */
    private void sendToKafka(List<Article> articles) {
        try {
            // 构建Kafka消息体
            Map<String, Object> kafkaMessage = new HashMap<>();
            kafkaMessage.put("operation", "bulk_index");
            kafkaMessage.put("index", INDEX_NAME);
            kafkaMessage.put("data", articles);
            kafkaMessage.put("timestamp", System.currentTimeMillis());

            // 发送到Kafka
            kafkaTemplate.send(KAFKA_TOPIC, kafkaMessage);
            System.out.println("数据已发送到Kafka，文章数量: " + articles.size());

        } catch (Exception e) {
            System.err.println("发送数据到Kafka失败: " + e.getMessage());
            throw new RuntimeException("Kafka发送失败", e);
        }
    }

    /**
     * 处理Kafka消息并批量写入ES
     * 实现Dubbo->Kafka->ES的数据流转第三步
     * 使用ES Bulk API提高写入性能
     * @param articles 从Kafka接收的文章数据
     * @return 成功写入的文档数量
     */
    public int processBulkIndexFromKafka(List<Article> articles) throws IOException {
        if (articles == null || articles.isEmpty()) {
            return 0;
        }

        // 构建批量操作请求
        List<BulkOperation> bulkOperations = new ArrayList<>();

        for (Article article : articles) {
            // 构建ES文档数据
            Map<String, Object> document = buildESDocument(article);

            // 创建索引操作
            IndexOperation<Map<String, Object>> indexOp = IndexOperation.of(i -> i
                    .index(INDEX_NAME)
                    .id(String.valueOf(article.getAid()))
                    .document(document)
            );

            // 添加到批量操作列表
            bulkOperations.add(BulkOperation.of(b -> b.index(indexOp)));
        }

        // 执行批量写入
        BulkRequest bulkRequest = BulkRequest.of(b -> b
                .index(INDEX_NAME)
                .operations(bulkOperations)
        );

        BulkResponse bulkResponse = elasticsearchClient.bulk(bulkRequest);

        // 统计成功写入的文档数量
        int successCount = 0;
        if (bulkResponse.errors()) {
            System.err.println("批量写入ES时发生错误");
            bulkResponse.items().forEach(item -> {
                if (item.error() != null) {
                    System.err.println("文档写入失败: " + item.error().reason());
                }
            });
        } else {
            successCount = bulkOperations.size();
            System.out.println("批量写入ES成功，文档数量: " + successCount);
        }

        return successCount;
    }

    /**
     * 全量数据同步到ES
     * 实现Dubbo->Kafka->ES的完整数据流转
     * 通过Bulk API提高写入性能
     * @return 操作结果信息
     */
    public Map<String, Object> fullSyncToES() {
        Map<String, Object> result = new HashMap<>();

        try {
            System.out.println("开始全量同步数据到ES...");

            // 第一步：从数据库查询所有文章数据
            List<Article> articles = docMapper.selectAllArticles();
            if (articles == null || articles.isEmpty()) {
                result.put("success", false);
                result.put("message", "数据库中没有文章数据");
                return result;
            }

            System.out.println("从数据库查询到 " + articles.size() + " 篇文章");

            // 第二步：发送数据到Kafka（模拟Dubbo调用）
            sendToKafka(articles);

            // 第三步：模拟从Kafka消费数据并写入ES
            // 在实际应用中，这部分应该在Kafka消费者中处理
            int successCount = processBulkIndexFromKafka(articles);

            // 返回结果
            result.put("success", true);
            result.put("message", "全量同步完成");
            result.put("totalCount", articles.size());
            result.put("successCount", successCount);

            System.out.println("全量同步ES完成，总文章数: " + articles.size() + "，成功写入: " + successCount);

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "全量同步失败: " + e.getMessage());
            System.err.println("全量同步ES失败: " + e.getMessage());
            e.printStackTrace();
        }

        return result;
    }

    /**
     * 保存单个文档到ES
     * @param aid 文章ID
     * @return 是否保存成功
     */
    public boolean saveDocument(Long aid) {
        try {
            // 从数据库查询文章
            Article article = docMapper.selectByAid(aid);
            if (article == null) {
                System.err.println("文章不存在: " + aid);
                return false;
            }

            // 构建ES文档
            Map<String, Object> document = buildESDocument(article);

            // 保存到ES
            IndexRequest<Map<String, Object>> request = IndexRequest.of(i -> i
                    .index(INDEX_NAME)
                    .id(String.valueOf(aid))
                    .document(document)
            );

            IndexResponse response = elasticsearchClient.index(request);
            boolean success = response.result() == Result.Created || response.result() == Result.Updated;

            if (success) {
                System.out.println("文档保存成功 ID: " + aid);
            }
            return success;

        } catch (IOException e) {
            System.err.println("保存文档时发生IO错误: " + e.getMessage());
            return false;
        } catch (ElasticsearchException e) {
            System.err.println("ES操作错误: " + e.getMessage());
            return false;
        }
    }

    /**
     * 检查文档是否存在于ES中
     * @param aid 文章ID
     * @return 是否存在
     */
    public boolean documentExists(Long aid) {
        try {
            return elasticsearchClient.exists(e -> e
                    .index(INDEX_NAME)
                    .id(String.valueOf(aid))
            ).value();
        } catch (Exception e) {
            System.err.println("检查文档存在性时出错: " + e.getMessage());
            return false;
        }
    }
}