package com.cjj.es.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticsearchConfig {    @Value("${spring.elasticsearch.uris}")
    private String elasticsearchUrl;

    @Value("${spring.elasticsearch.username:}")
    private String username;

    @Value("${spring.elasticsearch.password:}")
    private String password;

    @Bean
    public ElasticsearchClient elasticsearchClient() {
        // 解析 URL
        String host = "localhost";
        int port = 9200;
        String scheme = "http";
        
        if (elasticsearchUrl != null && !elasticsearchUrl.isEmpty()) {
            String url = elasticsearchUrl.replace("http://", "").replace("https://", "");
            String[] parts = url.split(":");
            if (parts.length >= 1) {
                host = parts[0];
            }
            if (parts.length >= 2) {
                port = Integer.parseInt(parts[1]);
            }
            if (elasticsearchUrl.startsWith("https://")) {
                scheme = "https";
            }
        }

        // 创建认证提供者
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
//        if (username != null && !username.equals("your_es_username") &&
//            password != null && !password.equals("your_es_password")) {
//            credentialsProvider.setCredentials(AuthScope.ANY,
//                    new UsernamePasswordCredentials(username, password));
//        }

        // 创建 RestClient
        RestClient restClient = RestClient.builder(new HttpHost(host, port, scheme))
                .setHttpClientConfigCallback(httpClientBuilder -> 
                        httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider))
                .build();

        // 创建传输层和客户端
        ElasticsearchTransport transport = new RestClientTransport(
                restClient, new JacksonJsonpMapper());

        return new ElasticsearchClient(transport);
    }
}