package com.lintao.blog.config;

import com.alibaba.fastjson.serializer.ToStringSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.lintao.blog.handler.LoginInterceptor;
import com.lintao.blog.handler.UpdateTokenInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.support.AllEncompassingFormHttpMessageConverter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WebMVCConfig implements WebMvcConfigurer {
    @Autowired
    private LoginInterceptor loginInterceptor;

    @Autowired
    private UpdateTokenInterceptor updateTokenInterceptor;
    /**
     * 跨域配置
     * @param registry
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        /*registry.addMapping("/**").allowedOrigins("http://localhost:8080");*/
        registry.addMapping("/**")
                .allowedOrigins("http://www.tzsblog.xyz/","null")
                .allowedOrigins("http://tzsblog.xyz/","null")
                .allowedOrigins("http://43.136.59.155/","null")
                .allowedMethods("POST","GET","PUT","OPTIONS","DELETE")
                .maxAge(3600)
                .allowCredentials(true);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(updateTokenInterceptor).addPathPatterns("/**");
        //先拦截test接口，后续遇到需要拦截的接口时再配置为真正的拦截接口
        registry.addInterceptor(loginInterceptor).addPathPatterns("/test")
                .addPathPatterns("/comments/create/change")
                .addPathPatterns("/articles/publish")
                .addPathPatterns("/users/update");
    }

    @Bean
    public CorsFilter corsFilter(){
        //1.添加CORS配置信息
        CorsConfiguration config = new CorsConfiguration();
        //1) 允许的域,不要写*，否则cookie就无法使用了
        config.addAllowedOrigin("http://tzsblog.xyz/");
        config.addAllowedOrigin("http://43.136.59.155/");
        config.addAllowedOrigin("http://www.tzsblog.xyz/");
        //3) 允许的请求方式
        config.addAllowedMethod("OPTIONS");
        config.addAllowedMethod("POST");
        config.addAllowedMethod("GET");
        // 4）允许的头信息
        config.addAllowedHeader("*");


        //初始化Cors配置源
        UrlBasedCorsConfigurationSource configSource = new UrlBasedCorsConfigurationSource();
        //2.添加映射路径，我们拦截一切请求
        configSource.registerCorsConfiguration("/**", config);

        //3.返回CorsFilter实例.参数:cors配置源
        return new CorsFilter(configSource);
    }
}
