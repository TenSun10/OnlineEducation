package com.tenxi.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "oe.auth")
public class AuthProperties {
    private List<String> includePaths = new ArrayList<>();
}
