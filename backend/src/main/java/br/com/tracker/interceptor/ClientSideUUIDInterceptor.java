package br.com.tracker.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class ClientSideUUIDInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("POST".equalsIgnoreCase(request.getMethod()) && request.getRequestURI().startsWith("/api/sessions")) {
            String clientUuid = request.getHeader("X-Client-UUID");
            if (clientUuid == null || clientUuid.trim().isEmpty()) {
                throw new IllegalArgumentException("O cabeçalho X-Client-UUID é obrigatório para esta operação.");
            }
        }
        return true;
    }
}
