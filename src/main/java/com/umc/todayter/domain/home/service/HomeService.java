package com.umc.todayter.domain.home.service;

import com.umc.todayter.domain.home.dto.response.HomeHeaderResponse;
import com.umc.todayter.domain.member.entity.Member;
import com.umc.todayter.domain.member.exception.MemberErrorCode;
import com.umc.todayter.domain.member.repository.MemberRepository;
import com.umc.todayter.global.apiPayload.exception.CustomException;
import com.umc.todayter.global.security.context.CurrentUserContext;
import com.umc.todayter.global.security.context.CurrentUserType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HomeService {

    private static final String DEFAULT_GREETING = "안녕하세요!";
    private static final String SUB_GREETING = "오늘도 좋은 기운 충전해요";

    private final MemberRepository memberRepository;
    private final Clock clock;

    public HomeHeaderResponse getHeader(CurrentUserContext context) {
        LocalDate date = LocalDate.now(clock);
        DayOfWeek dayOfWeek = date.getDayOfWeek();

        if (context.isMember()) {
            return getMemberHeader(context, date, dayOfWeek);
        }

        return new HomeHeaderResponse(
                CurrentUserType.GUEST,
                date,
                dayOfWeek,
                null,
                DEFAULT_GREETING,
                SUB_GREETING
        );
    }

    private HomeHeaderResponse getMemberHeader(CurrentUserContext context, LocalDate date, DayOfWeek dayOfWeek) {
        Member member = memberRepository
                .findById(context.memberId())
                .orElseThrow(() -> new CustomException(MemberErrorCode.MEMBER_NOT_FOUND));

        if (!member.isActive()) {
            throw new CustomException(MemberErrorCode.MEMBER_INACTIVE);
        }

        String nickname = normalizeNickname(member.getNickname());
        String greeting = nickname == null
                ? DEFAULT_GREETING
                : "안녕하세요 " + nickname + "님!";

        return new HomeHeaderResponse(
                CurrentUserType.MEMBER,
                date,
                dayOfWeek,
                nickname,
                greeting,
                SUB_GREETING
        );
    }

    private String normalizeNickname(String nickname) {
        return StringUtils.hasText(nickname)
                ? nickname.trim()
                : null;
    }
}
