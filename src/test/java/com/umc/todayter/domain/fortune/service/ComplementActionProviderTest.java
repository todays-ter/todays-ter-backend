package com.umc.todayter.domain.fortune.service;

import com.umc.todayter.domain.fortune.dto.response.FortuneReportResultResponse.ComplementActionGuide;
import com.umc.todayter.domain.fortune.enums.FiveElement;
import com.umc.todayter.domain.fortune.service.provider.ComplementActionProvider;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ComplementActionProviderTest {

    private final ComplementActionProvider provider = new ComplementActionProvider();

    @Test
    void selectsThreeStableActionsForEveryElement() {
        for (FiveElement element : FiveElement.values()) {
            ComplementActionGuide first = provider.select(element, 7L);
            ComplementActionGuide second = provider.select(element, 7L);

            assertThat(first.actions()).hasSize(3);
            assertThat(first.actions()).extracting("type").doesNotHaveDuplicates();
            assertThat(first).isEqualTo(second);
        }
    }
}
