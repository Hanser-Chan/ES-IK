package com.cjj.es.entity;

/**
 * 文章实体类
 * 对应数据库表 t_article
 * 遵循JavaBean规范：提供getter/setter方法
 */
public class Article {
    
    /**
     * 文档ID
     */
    private Long aid;
    
    /**
     * 文档标题
     */
    private String title;
    
    /**
     * 文档内容（HTML格式）
     */
    private String content;
    
    /**
     * 创建时间
     */
    private Long createTime;
    
    /**
     * 更新时间
     */
    private Long updateTime;
    
    /**
     * 文档类型：1为对内，2为对外
     */
    private Integer category;

    // 构造函数
    public Article() {}

    public Article(Long aid, String title, String content) {
        this.aid = aid;
        this.title = title;
        this.content = content;
    }

    // Getter和Setter方法
    public Long getAid() {
        return aid;
    }

    public void setAid(Long aid) {
        this.aid = aid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public Long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }

    public Integer getCategory() {
        return category;
    }

    public void setCategory(Integer category) {
        this.category = category;
    }

    @Override
    public String toString() {
        return "Article{" +
                "aid=" + aid +
                ", title='" + title + '\'' +
                ", content='" + (content != null ? content.substring(0, Math.min(50, content.length())) : null) + "...'" +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                ", category=" + category +
                '}';
    }
}
