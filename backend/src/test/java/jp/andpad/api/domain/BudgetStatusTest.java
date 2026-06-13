package jp.andpad.api.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import jp.andpad.api.domain.BudgetStatus;

class BudgetStatusTest {

    @Test
    void valuesAreDefined() {
        assertThat(BudgetStatus.values()).isNotEmpty();
        assertThat(BudgetStatus.valueOf("BudgetStatus.values()[0].name()")).isNotNull();
    }

}
