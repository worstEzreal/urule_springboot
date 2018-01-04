package com.example.demo;

import com.bstek.urule.console.servlet.URuleServlet;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * Desc
 *
 * @author zengxzh@yonyou.com
 * @version V1.0.0
 * @date 2018/1/3
 */
@Component
public class URuleServletRegistration {
    @Bean
    public ServletRegistrationBean registerURuleServlet(){
        return new ServletRegistrationBean(new URuleServlet(),"/urule/*");
    }
}