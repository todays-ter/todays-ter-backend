package com.umc.todayter.domain.fortune.service;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.umc.todayter.global.config.client.OpenAiFortuneReportClient;
import com.umc.todayter.global.config.client.AblecityManseClient;
import com.umc.todayter.domain.fortune.dto.internal.FortuneReportGenerationContext;
import com.umc.todayter.domain.fortune.exception.FortuneReportGenerationException;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import static org.mockito.Mockito.*;

class FortuneReportGenerationWorkerTest {

    private final FortuneReportProgressService progressService = mock(FortuneReportProgressService.class);
    private final AblecityManseClient ablecityClient = mock(AblecityManseClient.class);
    private final FortuneReportPromptProvider promptProvider = mock(FortuneReportPromptProvider.class);
    private final OpenAiFortuneReportClient openAiClient = mock(OpenAiFortuneReportClient.class);
    private final FortuneReportGenerationWorker worker = new FortuneReportGenerationWorker(
            progressService, ablecityClient, promptProvider, openAiClient
    );

    @Test
    void generateAdvancesAllStages() {
        FortuneReportGenerationContext context = mock(FortuneReportGenerationContext.class);
        JsonNode manseData = new ObjectMapper().createObjectNode();
        when(progressService.start(1L)).thenReturn(context);
        when(ablecityClient.calculate(context)).thenReturn(manseData);
        when(promptProvider.create(context, manseData)).thenReturn("prompt");
        when(openAiClient.generate("prompt")).thenReturn("report");

        worker.generate(1L);

        InOrder order = inOrder(progressService, ablecityClient, promptProvider, openAiClient);
        order.verify(progressService).start(1L);
        order.verify(ablecityClient).calculate(context);
        order.verify(progressService).saveManseData(1L, manseData);
        order.verify(promptProvider).create(context, manseData);
        order.verify(progressService).markPromptPrepared(1L);
        order.verify(openAiClient).generate("prompt");
        order.verify(progressService).saveAiReport(1L, "report");
        order.verify(progressService).complete(1L);
    }

    @Test
    void retryReusesSavedManseData() {
        FortuneReportGenerationContext context = mock(FortuneReportGenerationContext.class);
        JsonNode manseData = new ObjectMapper().createObjectNode();
        when(context.cachedManseData()).thenReturn(manseData);
        when(progressService.start(1L)).thenReturn(context);
        when(promptProvider.create(context, manseData)).thenReturn("prompt");
        when(openAiClient.generate("prompt")).thenReturn("report");

        worker.generate(1L);

        verifyNoInteractions(ablecityClient);
        verify(progressService, never()).saveManseData(anyLong(), any());
        verify(openAiClient).generate("prompt");
        verify(progressService).complete(1L);
    }

    @Test
    void generationFailureIsPersisted() {
        FortuneReportGenerationContext context = mock(FortuneReportGenerationContext.class);
        when(progressService.start(1L)).thenReturn(context);
        when(ablecityClient.calculate(context)).thenThrow(new FortuneReportGenerationException(
                "ABLECITY_API_FAILED", "만세력 정보를 생성하지 못했습니다."
        ));

        worker.generate(1L);

        verify(progressService).fail(1L, "ABLECITY_API_FAILED", "만세력 정보를 생성하지 못했습니다.");
        verifyNoInteractions(openAiClient);
    }
}
