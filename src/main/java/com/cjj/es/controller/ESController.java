package com.cjj.es.controller;

import com.cjj.es.service.ESService;
import com.cjj.es.service.ElasticsearchTestService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/doc")
public class ESController {

    private ElasticsearchTestService elasticsearchTestService;
    private ESService esService;
    /**
     * 全量数据同步接口 - saveDoc
     * 功能：将数据库数据全量用Bulk API写入ES
     * 数据流转路径：Dubbo->Kafka->ES
     * 接口映射：GET /init/es
     *
     * @return 操作结果信息
     */
    @GetMapping("/init/es")
    public Map<String, Object> saveDoc() {
        Map<String, Object> result = new HashMap<>();

        try {
            System.out.println("=== 开始执行saveDoc全量同步 ===");

            // 检查ES连接状态
            if (!elasticsearchTestService.isConnected()) {
                result.put("success", false);
                result.put("message", "ES连接失败，无法执行同步操作");
                System.err.println("ES连接检查失败");
                return result;
            }

            // 调用ESService执行全量同步
            // 内部实现：数据库查询 -> Kafka发送 -> ES批量写入

            Map<String, Object> syncResult = esService.fullSyncToES();

            // 记录操作日志
            boolean success = (Boolean) syncResult.get("success");
            if (success) {
                Integer totalCount = (Integer) syncResult.get("totalCount");
                Integer successCount = (Integer) syncResult.get("successCount");
                System.out.println("saveDoc执行成功 - 总数据: " + totalCount + ", 成功写入: " + successCount);
            } else {
                System.err.println("saveDoc执行失败: " + syncResult.get("message"));
            }

            return syncResult;

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "saveDoc执行异常: " + e.getMessage());
            System.err.println("saveDoc执行过程中发生异常: " + e.getMessage());
            e.printStackTrace();
            return result;
        }
    }
}
