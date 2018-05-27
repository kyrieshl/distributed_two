package com.litemall.distributed_two.config;

import com.litemall.distributed_two.annotation.support.LoginAdminHandlerMethodArgumentResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.util.List;

@Configuration
public class AdminConfig extends WebMvcConfigurerAdapter {

    @Autowired
    private LoginAdminHandlerMethodArgumentResolver loginAdminHandlerMethodArgumentResolver;
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(loginAdminHandlerMethodArgumentResolver);
    }
}
