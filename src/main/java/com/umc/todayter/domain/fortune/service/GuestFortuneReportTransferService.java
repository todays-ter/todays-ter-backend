package com.umc.todayter.domain.fortune.service;

import com.umc.todayter.domain.fortune.repository.FortuneReportRepository;
import com.umc.todayter.domain.member.entity.Member;
import com.umc.todayter.domain.onboarding.entity.GuestSession;
import com.umc.todayter.domain.onboarding.repository.GuestSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GuestFortuneReportTransferService {

    private final GuestSessionRepository guestSessionRepository;
    private final FortuneReportRepository fortuneReportRepository;

    @Transactional
    public void transferIfPresent(String guestId, Member member) {
        if (guestId == null || guestId.isBlank()) {
            return;
        }

        GuestSession guestSession = guestSessionRepository.findForUpdateByGuestId(guestId).orElse(null);
        if (guestSession == null || !guestSession.isUsable(LocalDateTime.now())) {
            return;
        }

        fortuneReportRepository.findAllByGuestSessionId(guestSession.getId())
                .forEach(report -> report.transferToMember(member.getId()));
    }
}
