package com.cjj.es.mapper;

import com.cjj.es.entity.Article;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 文章数据访问接口
 * 负责从MySQL数据库读取文章数据
 * 遵循单一职责原则：专注于数据库查询操作
 */
@Mapper
public interface DocMapper {

    /**
     * 查询所有文章数据
     * 用于全量同步到ES
     * @return 所有文章列表
     */
    @Select("SELECT aid, title, content, create_time, update_time, category FROM t_article")
    List<Article> selectAllArticles();

    /**
     * 根据ID查询单篇文章
     * @param aid 文章ID
     * @return 文章实体
     */
    @Select("SELECT aid, title, content, create_time, update_time, category FROM t_article WHERE aid = #{aid}")
    Article selectByAid(Long aid);

    /**
     * 分页查询文章
     * 用于大数据量时的分批处理
     * @param offset 偏移量
     * @param limit 每页数量
     * @return 文章列表
     */
    @Select("SELECT aid, title, content, create_time, update_time, category FROM t_article LIMIT #{offset}, #{limit}")
    List<Article> selectByPage(int offset, int limit);

    /**
     * 统计文章总数
     * @return 文章总数
     */
    @Select("SELECT COUNT(*) FROM t_article")
    int countArticles();
}
