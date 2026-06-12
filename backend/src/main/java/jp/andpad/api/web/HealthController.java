package jp.andpad.api.web;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import jp.andpad.api.domain.Health;

@RestController
public class HealthController {

    @GetMapping("/health")
    public Health health() {
        return new Health(true, "andpad-api", "2.0.0-saas");
    }

    @GetMapping("/status")
    public Map<String, Object> status() {
        return Map.of(
                "service", "andpad-api",
                "ok", true,
                "postgres", true);
    }
}
