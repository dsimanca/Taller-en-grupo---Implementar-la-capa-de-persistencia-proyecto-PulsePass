package com.pulsepass.migration;

import static org.assertj.core.api.Assertions.assertThat;

import com.pulsepass.BaseIntegrationTest;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class FlywayMigrationIT extends BaseIntegrationTest {
    @Autowired private Flyway flyway;

    @Test
    void shouldApplyAllMigrationsFromEmptyDatabase() {
        assertThat(flyway.info().applied()).hasSizeGreaterThanOrEqualTo(3);
    }
}

