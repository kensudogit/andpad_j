package jp.andpad.api.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import jp.andpad.api.domain.LearningActivityKind;

class LearningActivityKindTest {

    @Test
    void valuesAreDefined() {
        assertThat(LearningActivityKind.values()).isNotEmpty();
        assertThat(LearningActivityKind.valueOf("LearningActivityKind.values()[0].name()")).isNotNull();
    }

}
