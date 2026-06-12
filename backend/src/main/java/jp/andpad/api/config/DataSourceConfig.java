package jp.andpad.api.config;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.util.StringUtils;

import com.zaxxer.hikari.HikariDataSource;

/** Railway の postgresql:// URL を Spring JDBC 向け jdbc:postgresql:// に正規化する。 */
@Configuration
public class DataSourceConfig {

    private static final Pattern PG_URL =
            Pattern.compile("^postgresql://([^/@]+@)?([^/?]+)(\\?.*)?$");

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource")
    DataSourceProperties dataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @Primary
    DataSource dataSource(DataSourceProperties props) {
        String url = props.getUrl();
        if (url != null && url.startsWith("postgresql://")) {
            applyCredentialsFromUrl(props, url);
            url = "jdbc:" + url;
            if (!url.contains("sslmode=")) {
                url += url.contains("?") ? "&sslmode=require" : "?sslmode=require";
            }
            props.setUrl(url);
        }
        return props.initializeDataSourceBuilder().type(HikariDataSource.class).build();
    }

    private static void applyCredentialsFromUrl(DataSourceProperties props, String url) {
        if (StringUtils.hasText(System.getenv("DB_USER")) || StringUtils.hasText(System.getenv("DB_PASSWORD"))) {
            return;
        }
        Matcher matcher = PG_URL.matcher(url);
        if (!matcher.find()) {
            return;
        }
        String userInfo = matcher.group(1);
        if (!StringUtils.hasText(userInfo)) {
            return;
        }
        userInfo = userInfo.substring(0, userInfo.length() - 1);
        String[] parts = userInfo.split(":", 2);
        if (parts.length > 0 && StringUtils.hasText(parts[0])) {
            props.setUsername(decode(parts[0]));
        }
        if (parts.length > 1) {
            props.setPassword(decode(parts[1]));
        }
    }

    private static String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }
}
