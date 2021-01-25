package com.lrm.interceptor;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
public class WebConfig extends WebMvcConfigurerAdapter
{
    @Override
    public void addInterceptors(InterceptorRegistry registry)
    {
        registry.addInterceptor(new LoginInterceptor())
                //注意路径包括 /admin/
                .addPathPatterns("/admin/**")
                //输入admin之后按照正常的话会调用/admin的getmapping方法 返回admin/login视图 如果不排除就死循环在admin进不去了
                .excludePathPatterns("/admin")
                //并且因为提交表单要提交到login登录页面，因为你没登录，所以要提交，但是没登录又提交不上去，所以要排除
                .excludePathPatterns("/admin/login");
    }
}
