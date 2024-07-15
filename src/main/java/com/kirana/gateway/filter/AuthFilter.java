package com.kirana.gateway.filter;

import com.kirana.gateway.constants.Constants;
import com.kirana.gateway.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ServerWebExchange;

import java.util.Objects;

@Component
public class AuthFilter extends AbstractGatewayFilterFactory<AuthFilter.Config> {

    @Autowired
    private RouteValidator validator;

    @Autowired
    private RestTemplate template;

    @Autowired
    private JwtUtil jwtUtil;

    public AuthFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return ((exchange, chain) -> {
            if (validator.isSecured.test(exchange.getRequest())) {
                if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                    throw new RuntimeException("Missing Auth Header");
                }
                try {
                    String authHeader = Objects.requireNonNull(exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION)).get(0);
                    if (authHeader != null && authHeader.startsWith("Bearer")) {
                        authHeader = authHeader.substring(7);
                    } else {
                        throw new RuntimeException("No Auth token provided");
                    }
                    jwtUtil.validateToken(authHeader);
                    ServerHttpRequest request = exchange.getRequest()
                            .mutate()
                            .header(Constants.X_USER_ID, jwtUtil.getUserId(authHeader))
                            .build();
                    ServerWebExchange modExchange = exchange.mutate().request(request).build();
                    return chain.filter(modExchange);
                } catch (Exception e) {
                    throw new RuntimeException("unauthorized");
                }
            }

            return chain.filter(exchange);
        });
    }

    public static class Config {

    }

}
