package com.qg.server.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * MyBatis-Plus 核心配置
 * 1. 分页插件
 * 2. 单条/批量 执行器分离（解决批量慢 + 主键回填问题）
 */
@Configuration
public class MybatisPlusConfiguration {

    /**
     * 分页插件（必须配置，否则分页不生效）
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        PaginationInnerInterceptor paginationInterceptor = new PaginationInnerInterceptor(DbType.MYSQL);
        // 设置最大单页限制数量，-1 不受限制
        paginationInterceptor.setMaxLimit(-1L);
        interceptor.addInnerInterceptor(paginationInterceptor);
        return interceptor;
    }

    /**
     * 默认 SqlSession：单条操作，支持主键回填
     */
    @Bean
    @Primary
    public SqlSessionTemplate sqlSessionTemplate(SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory, ExecutorType.SIMPLE);
    }

    /**
     * 批量专用 SqlSession：高性能批量处理
     * 使用方式：@Resource(name = "sqlSessionTemplateBatch")
     */
    @Bean("sqlSessionTemplateBatch")
    public SqlSessionTemplate sqlSessionTemplateBatch(SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory, ExecutorType.BATCH);
    }
}