// package com.aaspaas.aaspaas_backend.config;

// import com.aaspaas.aaspaas_backend.security.ApiRateLimitFilter;

// import org.springframework.boot.web.servlet.FilterRegistrationBean;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;

// @Configuration
// public class RateLimitFilterRegistrationConfig {

//     @Bean
//     public FilterRegistrationBean<ApiRateLimitFilter>
//     rateLimitFilterServletRegistration(ApiRateLimitFilter filter) {

//         FilterRegistrationBean<ApiRateLimitFilter> registration =
//                 new FilterRegistrationBean<>();

//         registration.setFilter(filter);
//         registration.setEnabled(false);

//         return registration;
//     }
// }

package com.aaspaas.aaspaas_backend.config;

import com.aaspaas.aaspaas_backend.security.ApiRateLimitFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RateLimitFilterRegistrationConfig {

    @Bean
    public FilterRegistrationBean<ApiRateLimitFilter>
    disableServletContainerRegistration(ApiRateLimitFilter filter) {

        FilterRegistrationBean<ApiRateLimitFilter> registration =
                new FilterRegistrationBean<>(filter);

        registration.setEnabled(false);
        return registration;
    }
}