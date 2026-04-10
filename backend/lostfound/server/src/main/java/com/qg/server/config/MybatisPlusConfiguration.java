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
 * MyBatis-Plus 配置
 * - 分页插件
 * - 单条插入使用 SIMPLE 执行器
 * - 批量操作使用 BATCH 执行器
 */
@Configuration
public class MybatisPlusConfiguration {

    /**
     * MyBatis-Plus 分页插件
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }

    /**
     * 默认 SqlSessionTemplate，用于单条插入/更新
     * 保证自增主键回填正常
     */
    @Bean
    @Primary
    public SqlSessionTemplate sqlSessionTemplateSimple(SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory, ExecutorType.SIMPLE);
    }

    /**
     * 批量操作 SqlSessionTemplate
     * 只在批量 insert/update/delete 时使用
     */
    @Bean("sqlSessionTemplateBatch")
    public SqlSessionTemplate sqlSessionTemplateBatch(SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory, ExecutorType.BATCH);
    }
}
