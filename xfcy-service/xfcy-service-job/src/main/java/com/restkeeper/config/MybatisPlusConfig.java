package com.restkeeper.config;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.spring.MybatisSqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.mybatis.spring.transaction.SpringManagedTransactionFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
@MapperScan("com.restkeeper.mapper")
public class MybatisPlusConfig{
    @Value("${spring.datasource.order.driver-class-name}")
    private String driverClassName;

    @Value("${spring.datasource.order.password}")
    private String password;

    @Value("${spring.datasource.order.url}")
    private String url;

    @Value("${spring.datasource.order.username}")
    private String username;

    @Bean(name = "orderDataSource")
    public DataSource orderDataSource(){
        return DataSourceBuilder.create()
                .driverClassName(driverClassName)
                .url(url)
                .password(password)
                .username(username)
                .build();
    }

    @Bean("sqlSessionFactory")
    public MybatisSqlSessionFactoryBean sqlSessionFactory(@Qualifier("orderDataSource") DataSource dataSource) throws Exception {

        // 这里必须用 MybatisSqlSessionFactoryBean 代替了 SqlSessionFactoryBean，否则 MyBatisPlus 不会生效
        MybatisSqlSessionFactoryBean mybatisSqlSessionFactoryBean = new MybatisSqlSessionFactoryBean();
        mybatisSqlSessionFactoryBean.setDataSource(dataSource);
        mybatisSqlSessionFactoryBean.setTransactionFactory(new SpringManagedTransactionFactory());

        GlobalConfig globalConfig  = new GlobalConfig();


        mybatisSqlSessionFactoryBean.setGlobalConfig(globalConfig);

        //本模块 mapper 全部使用注解 SQL，无需 XML；显式指定 classpath*:/mapper/*.xml 匹配不到资源，
        //在 Spring 7（Boot 4）下会因 NestedIOException 被移除而创建 sqlSessionFactory 失败。

        //MyBatis-Plus 3.5.x 的 MybatisPlusProperties.CoreConfiguration 与 core.MybatisConfiguration
        //已非继承关系，无法直接转换；本模块使用显式配置即可。
        MybatisConfiguration configuration = new MybatisConfiguration();
        configuration.setMapUnderscoreToCamelCase(Boolean.TRUE);
        mybatisSqlSessionFactoryBean.setConfiguration(configuration);

        return mybatisSqlSessionFactoryBean;
    }
}

