package com.umc.todayter.domain.fortune.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.umc.todayter.domain.fortune.client.OpenAiFortuneReportClient;
import com.umc.todayter.domain.fortune.client.SazuManseClient;
import com.umc.todayter.domain.fortune.dto.internal.FortuneReportGenerationContext;
import com.umc.todayter.domain.fortune.exception.FortuneReportGenerationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FortuneReportGenerationWorker {

    private final FortuneReportProgressService progressService;
    private final SazuManseClient sazuManseClient;
    private final FortuneReportPromptProvider promptProvider;
    private final OpenAiFortuneReportClient openAiClient;

    public void generate(Long reportId) {
        try {
            FortuneReportGenerationContext context = progressService.start(reportId);

            JsonNode manseData = sazuManseClient.calculate(context);
            progressService.saveManseData(reportId, manseData);

            String prompt = promptProvider.create(context, manseData);
            progressService.markPromptPrepared(reportId);

            String reportContent = openAiClient.generate(prompt);
            progressService.saveAiReport(reportId, reportContent);

            progressService.complete(reportId);
        } catch (FortuneReportGenerationException e) {
            log.warn("사주 리포트 생성을 실패했습니다. reportId={}, code={}", reportId, e.getFailureCode(), e);
            progressService.fail(reportId, e.getFailureCode(), e.getPublicMessage());
        } catch (Exception e) {
            log.error("예기치 못한 사주 리포트 생성 실패했습니다. reportId={}", reportId, e);
            progressService.fail(reportId, "REPORT_GENERATION_FAILED",
                    "리포트 생성 중 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.");
        }
    }
}
