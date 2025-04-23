package com.fq.yznu.eaps.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.lang.NonNull;

/**
 * Web MVC配置类
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

        /**
         * 配置静态资源访问
         */
        @Override
        public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
                // 配置Swagger UI资源
                registry.addResourceHandler("swagger-ui.html")
                                .addResourceLocations("classpath:/META-INF/resources/");

                registry.addResourceHandler("/webjars/**")
                                .addResourceLocations("classpath:/META-INF/resources/webjars/");
        }
}