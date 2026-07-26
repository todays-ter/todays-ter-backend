package com.umc.todayter.domain.fortune.event;

import com.umc.todayter.domain.fortune.service.FortuneReportGenerationWorker;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class FortuneReportGenerationEventListener {

    private final FortuneReportGenerationWorker worker;

    @Async("fortuneReportExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(FortuneReportGenerationRequestedEvent event) {
        worker.generate(event.reportId());
    }
}
