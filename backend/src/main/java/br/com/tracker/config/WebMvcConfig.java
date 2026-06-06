package br.com.tracker.config;

import br.com.tracker.interceptor.ClientSideUUIDInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final ClientSideUUIDInterceptor clientSideUUIDInterceptor;

    public WebMvcConfig(ClientSideUUIDInterceptor clientSideUUIDInterceptor) {
        this.clientSideUUIDInterceptor = clientSideUUIDInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(clientSideUUIDInterceptor)
                .addPathPatterns("/api/sessions");
    }
}
