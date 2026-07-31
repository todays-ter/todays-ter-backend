package com.umc.todayter.domain.home.service;

import com.umc.todayter.domain.home.dto.response.HomeHeaderResponse;
import com.umc.todayter.domain.member.entity.Member;
import com.umc.todayter.domain.member.enums.MemberStatus;
import com.umc.todayter.domain.member.exception.MemberErrorCode;
import com.umc.todayter.domain.member.repository.MemberRepository;
import com.umc.todayter.global.apiPayload.exception.CustomException;
import com.umc.todayter.global.security.context.CurrentUserContext;
import com.umc.todayter.global.security.context.CurrentUserType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HomeServiceTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(
            Instant.parse("2026-07-31T01:00:00Z"),
            ZoneId.of("Asia/Seoul")
    );

    @Mock
    private MemberRepository memberRepository;

    @Test
    void memberHeaderReturnsGreetingWithNickname() {
        Member member = member(1L, "현우");
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

        HomeHeaderResponse response = service().getHeader(CurrentUserContext.forMember(1L));

        assertThat(response.userType()).isEqualTo(CurrentUserType.MEMBER);
        assertThat(response.date()).isEqualTo(LocalDate.of(2026, 7, 31));
        assertThat(response.dayOfWeek()).isEqualTo(DayOfWeek.FRIDAY);
        assertThat(response.nickname()).isEqualTo("현우");
        assertThat(response.greeting()).isEqualTo("안녕하세요 현우님!");
        assertThat(response.subGreeting()).isEqualTo("오늘도 좋은 기운 충전해요");
        verify(memberRepository).findById(1L);
    }

    @Test
    void memberNicknameNullReturnsDefaultGreeting() {
        assertNicknameFallback(null);
    }

    @Test
    void memberNicknameEmptyReturnsDefaultGreeting() {
        assertNicknameFallback("");
    }

    @Test
    void memberNicknameBlankReturnsDefaultGreeting() {
        assertNicknameFallback("   ");
    }

    @Test
    void memberNicknameIsTrimmed() {
        Member member = member(1L, " 현우 ");
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

        HomeHeaderResponse response = service().getHeader(CurrentUserContext.forMember(1L));

        assertThat(response.nickname()).isEqualTo("현우");
        assertThat(response.greeting()).isEqualTo("안녕하세요 현우님!");
    }

    @Test
    void guestHeaderDoesNotQueryMemberRepository() {
        HomeHeaderResponse response = service().getHeader(CurrentUserContext.forGuest(10L, "guest-id"));

        assertThat(response.userType()).isEqualTo(CurrentUserType.GUEST);
        assertThat(response.date()).isEqualTo(LocalDate.of(2026, 7, 31));
        assertThat(response.dayOfWeek()).isEqualTo(DayOfWeek.FRIDAY);
        assertThat(response.nickname()).isNull();
        assertThat(response.greeting()).isEqualTo("안녕하세요!");
        assertThat(response.subGreeting()).isEqualTo("오늘도 좋은 기운 충전해요");
        verify(memberRepository, never()).findById(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void missingMemberThrowsMemberNotFound() {
        when(memberRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service().getHeader(CurrentUserContext.forMember(1L)))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(MemberErrorCode.MEMBER_NOT_FOUND));
    }

    @Test
    void inactiveMemberThrowsMemberInactive() {
        Member member = member(1L, "현우");
        ReflectionTestUtils.setField(member, "status", MemberStatus.WITHDRAWN);
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

        assertThatThrownBy(() -> service().getHeader(CurrentUserContext.forMember(1L)))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                        .isEqualTo(MemberErrorCode.MEMBER_INACTIVE));
    }

    private HomeService service() {
        return new HomeService(memberRepository, FIXED_CLOCK);
    }

    private void assertNicknameFallback(String nickname) {
        Member member = member(1L, nickname);
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

        HomeHeaderResponse response = service().getHeader(CurrentUserContext.forMember(1L));

        assertThat(response.nickname()).isNull();
        assertThat(response.greeting()).isEqualTo("안녕하세요!");
    }

    private Member member(Long id, String nickname) {
        Member member = Member.create(nickname);
        ReflectionTestUtils.setField(member, "id", id);
        return member;
    }
}
