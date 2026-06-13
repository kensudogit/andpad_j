package jp.andpad.api.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class DatabaseUrlSupportTest {

    @Test
    void railwayInternalUsesSslDisable() {
        String raw = "postgresql://postgres:secret@postgres.railway.internal:5432/railway";
        String jdbc = DatabaseUrlSupport.normalizeForJdbc(raw);
        assertThat(jdbc).contains("sslmode=disable");
        assertThat(jdbc).startsWith("jdbc:postgresql://");
    }

    @Test
    void publicRailwayHostUsesSslRequire() {
        String raw = "postgresql://postgres:secret@containers-us-west-123.railway.app:6543/railway";
        String jdbc = DatabaseUrlSupport.normalizeForJdbc(raw);
        assertThat(jdbc).contains("sslmode=require");
    }
}
