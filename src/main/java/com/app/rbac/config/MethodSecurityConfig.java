package com.app.rbac.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

import com.app.rbac.security.CustomPermissionEvaluator;

@Configuration
@EnableMethodSecurity(prePostEnabled = true)
public class MethodSecurityConfig {
	
	  @Bean
	    static MethodSecurityExpressionHandler methodSecurityExpressionHandler(CustomPermissionEvaluator permissionEvaluator) {

	        DefaultMethodSecurityExpressionHandler handler = new DefaultMethodSecurityExpressionHandler();
	        handler.setPermissionEvaluator(permissionEvaluator);
	        return handler;
	    }

}
