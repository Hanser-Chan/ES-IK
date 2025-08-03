package com.cjj.es.controller;

import com.cjj.es.service.ElasticsearchTestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class ElasticsearchTestController {

    @Autowired
    private ElasticsearchTestService elasticsearchTestService;

    /**
     * 手动测试连接
     */
//    public void testConnection() {
//        elasticsearchTestService.testConnection();
//    }
    
    /**
     * 检查连接状态
     */
    public boolean checkConnection() {
        return elasticsearchTestService.isConnected();
    }


}