package cn.codeprobe.blog.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class Swagger2Config {

    //版本
    private static final String VERSION = "1.0.0";

    /**
     * 是否开启swagger
     */
    @Value("${swagger.enabled}")
    private boolean enabled;


    /**
     * 门户API，接口前缀：portal
     *
     * @return
     */
    @Bean
    public Docket portalApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                // 是否启用Swagger
                .enable(enabled)
                // 用来创建该API的基本信息，展示在文档的页面中（自定义展示的信息）
                .apiInfo(portalApiInfo())
                // 设置哪些接口暴露给Swagger展示
                .select()
                // 扫描所有有注解的api，用这种方式更灵活
                //.apis(RequestHandlerSelectors.withMethodAnnotation(ApiOperation.class))
                // 扫描指定包中的swagger注解
                .apis(RequestHandlerSelectors.basePackage("cn.codeprobe.blog.controller.portal"))
                // 扫描所有 .apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.any()) // 可以根据url路径设置哪些请求加入文档，忽略哪些请求
                .build()
                .groupName("前端门户");
    }

    /**
     * 添加摘要信息
     */
    private ApiInfo portalApiInfo() {

        Contact contact = new Contact("lionido", "www,yzjblog.com", "codeprobe@163.com");

        return new ApiInfoBuilder()
                // 设置标题
                .title("博客系统-门户接口文档")
                // 设置文档的描述
                .description("门户接口文档")
                // 设置文档的版本信息-> 1.0.0 Version information
                .version(VERSION)
                // 作者信息
                .contact(contact)
                .build();
    }


    /**
     * 管理中心api，接口前缀：admin
     *
     * @return
     */
    @Bean
    public Docket adminApi() {
        return new Docket(DocumentationType.SWAGGER_12)
                .enable(enabled)
                .apiInfo(adminApiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("cn.codeprobe.blog.controller.admin"))
                .paths(PathSelectors.any())
                .build()
                .groupName("管理中心");
    }


    private ApiInfo adminApiInfo() {
        return new ApiInfoBuilder()
                .title("博客系统-管理中心接口文档")
                .description("管理中心接口")
                .version(VERSION)
                .build();
    }


    /**
     * 管理中心api，接口前缀：admin
     *
     * @return
     */
    @Bean
    public Docket UserApi() {
        return new Docket(DocumentationType.SWAGGER_12)
                .enable(enabled)
                .apiInfo(userApiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("cn.codeprobe.blog.controller.user"))
                .paths(PathSelectors.any())
                .build()
                .groupName("用户中心");
    }

    private ApiInfo userApiInfo() {
        return new ApiInfoBuilder()
                .title("博客系统-用户接口文档")
                .description("用户接口")
                .version(VERSION)
                .build();
    }



}