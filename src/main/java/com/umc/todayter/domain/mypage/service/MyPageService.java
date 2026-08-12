package com.umc.todayter.domain.mypage.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.umc.todayter.domain.fortune.entity.FortuneReport;
import com.umc.todayter.domain.fortune.enums.FiveElement;
import com.umc.todayter.domain.fortune.repository.FortuneReportRepository;
import com.umc.todayter.domain.member.entity.Member;
import com.umc.todayter.domain.member.repository.MemberRepository;
import com.umc.todayter.domain.member.repository.SocialAccountRepository;
import com.umc.todayter.domain.mypage.dto.MyPageRequestDTO;
import com.umc.todayter.domain.mypage.dto.MyPageResponseDTO;
import com.umc.todayter.domain.mypage.entity.MyPageNotificationSetting;
import com.umc.todayter.domain.mypage.entity.PolicyType;
import com.umc.todayter.domain.mypage.entity.UserPermission;
import com.umc.todayter.domain.mypage.entity.UserPolicyAgreement;
import com.umc.todayter.domain.mypage.repository.MyPageNotificationSettingRepository;
import com.umc.todayter.domain.mypage.repository.UserPermissionRepository;
import com.umc.todayter.domain.mypage.repository.UserPolicyAgreementRepository;
import com.umc.todayter.global.apiPayload.response.ErrorCode;
import com.umc.todayter.global.apiPayload.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MyPageService {

    private final MemberRepository memberRepository;
    private final SocialAccountRepository socialAccountRepository;
    private final FortuneReportRepository fortuneReportRepository;
    private final MyPageNotificationSettingRepository myPageNotificationSettingRepository;
    private final UserPermissionRepository userPermissionRepository;
    private final UserPolicyAgreementRepository userPolicyAgreementRepository;
    private final ObjectMapper objectMapper; // JSON 파싱용 ObjectMapper 추가

    // 0. 마이페이지 메인 조회 (GET /mypage)
    @Transactional(readOnly = true)
    public MyPageResponseDTO.MainDTO getMyPage(Long userId) {
        // 1) Member 조회
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        // 2) 최신 FortuneReport 조회
        FortuneReport latestReport = fortuneReportRepository.findFirstByMemberIdOrderByCreatedAtDescIdDesc(userId)
                .orElse(null);

        Long reportId = (latestReport != null) ? latestReport.getId() : null;
        String mainElement = null;
        String complementaryElement = null;

        // 3) manseData JSON에서 오행 정보 추출
        if (latestReport != null && latestReport.getManseData() != null) {
            try {
                JsonNode jsonNode = objectMapper.readTree(latestReport.getManseData());
                
                // manseData JSON 내 Key 값에 맞춰 가져옵니다 (예: "WATER", "FIRE" 등의 Enum 이름 또는 "수", "화" 등의 label)
                if (jsonNode.has("mainElement")) {
                    String rawMain = jsonNode.get("mainElement").asText();
                    FiveElement element = FiveElement.fromLabel(rawMain);
                    mainElement = (element != null) ? element.getLabel() : rawMain;
                }
                
                if (jsonNode.has("complementaryElement")) {
                    String rawComp = jsonNode.get("complementaryElement").asText();
                    FiveElement element = FiveElement.fromLabel(rawComp);
                    complementaryElement = (element != null) ? element.getLabel() : rawComp;
                }
            } catch (Exception e) {
                // JSON 파싱 실패 시 null 처리 (에러 방지)
            }
        }

        // 4) 소셜 로그인 Provider 조회
        String loginProvider = socialAccountRepository.findAllByMemberIdOrderByIdAsc(userId)
                .stream()
                .findFirst()
                .map(socialAccount -> socialAccount.getProvider().name())
                .orElse(null);

        // 5) Response DTO 생성
        return MyPageResponseDTO.MainDTO.builder()
                .reportId(reportId)
                .nickname(member.getNickname())
                .profileImageUrl(null)
                .userId(member.getId())
                .loginProvider(loginProvider)
                .mainElement(mainElement)
                .complementaryElement(complementaryElement)
                .build();
    }

    // 1. 마이페이지 알림 설정 조회
    @Transactional(readOnly = true)
    public MyPageResponseDTO.NotificationSettingDTO getNotificationSettings(Long userId) {
        MyPageNotificationSetting setting = getOrCreateNotificationSetting(userId);
        return MyPageResponseDTO.NotificationSettingDTO.builder()
                .isPushEnabled(setting.isPushEnabled())
                .isMarketingEnabled(setting.isMarketingEnabled())
                .isNightMarketingEnabled(setting.isNightMarketingEnabled())
                .build();
    }

    // 2. 마이페이지 알림 설정 변경
    @Transactional
    public MyPageResponseDTO.UpdateResultDTO updateNotificationSettings(Long userId, MyPageRequestDTO.UpdateNotificationSettingDTO request) {
        MyPageNotificationSetting setting = getOrCreateNotificationSetting(userId);
        setting.updateSettings(
                request.isPushEnabled(),
                request.isMarketingEnabled(),
                request.isNightMarketingEnabled()
        );
        return MyPageResponseDTO.UpdateResultDTO.builder()
                .updatedAt(setting.getUpdatedAt())
                .build();
    }

    // 3. 권한 설정 조회
    @Transactional(readOnly = true)
    public MyPageResponseDTO.PermissionDTO getPermissions(Long userId) {
        UserPermission permission = getOrCreateUserPermission(userId);
        return MyPageResponseDTO.PermissionDTO.builder()
                .isCameraAllowed(permission.isCameraAllowed())
                .isPhotoLibraryAllowed(permission.isPhotoLibraryAllowed())
                .isLocationAllowed(permission.isLocationAllowed())
                .build();
    }

    // 4. 권한 설정 변경
    @Transactional
    public MyPageResponseDTO.UpdateResultDTO updatePermissions(Long userId, MyPageRequestDTO.UpdatePermissionDTO request) {
        UserPermission permission = getOrCreateUserPermission(userId);
        permission.updatePermissions(
                request.isCameraAllowed(),
                request.isPhotoLibraryAllowed(),
                request.isLocationAllowed()
        );
        return MyPageResponseDTO.UpdateResultDTO.builder()
                .updatedAt(permission.getUpdatedAt())
                .build();
    }

    // 5. 개인정보 및 약관 목록 조회
    @Transactional(readOnly = true)
    public MyPageResponseDTO.PolicyListDTO getPolicies(Long userId) {
        List<UserPolicyAgreement> agreements = userPolicyAgreementRepository.findByUserId(userId);

        if (agreements.isEmpty()) {
            agreements = initDefaultPolicies(userId);
        }

        List<MyPageResponseDTO.PolicyDTO> policyDTOs = agreements.stream()
                .map(agreement -> MyPageResponseDTO.PolicyDTO.builder()
                        .type(agreement.getType())
                        .title(agreement.getTitle())
                        .url(agreement.getUrl())
                        .isRequired(agreement.isRequired())
                        .isAgreed(agreement.isAgreed())
                        .agreedAt(agreement.getAgreedAt())
                        .build())
                .toList();

        return MyPageResponseDTO.PolicyListDTO.builder()
                .policies(policyDTOs)
                .build();
    }

    private MyPageNotificationSetting getOrCreateNotificationSetting(Long userId) {
        return myPageNotificationSettingRepository.findByUserId(userId)
                .orElseGet(() -> myPageNotificationSettingRepository.save(
                        MyPageNotificationSetting.builder()
                                .userId(userId)
                                .isPushEnabled(true)
                                .isMarketingEnabled(false)
                                .isNightMarketingEnabled(false)
                                .updatedAt(LocalDateTime.now())
                                .build()
                ));
    }

    private UserPermission getOrCreateUserPermission(Long userId) {
        return userPermissionRepository.findByUserId(userId)
                .orElseGet(() -> userPermissionRepository.save(
                        UserPermission.builder()
                                .userId(userId)
                                .isCameraAllowed(true)
                                .isPhotoLibraryAllowed(false)
                                .isLocationAllowed(false)
                                .updatedAt(LocalDateTime.now())
                                .build()
                ));
    }

    @Transactional
    private List<UserPolicyAgreement> initDefaultPolicies(Long userId) {
        List<UserPolicyAgreement> defaults = List.of(
                UserPolicyAgreement.builder()
                        .userId(userId)
                        .type(PolicyType.TERMS_OF_SERVICE)
                        .title("서비스 이용약관")
                        .url("https://domain.com/policies/terms")
                        .isRequired(true)
                        .isAgreed(true)
                        .agreedAt(LocalDateTime.of(2025, 1, 1, 10, 0, 0))
                        .build(),
                UserPolicyAgreement.builder()
                        .userId(userId)
                        .type(PolicyType.PRIVACY_POLICY)
                        .title("개인정보 처리방침")
                        .url("https://domain.com/policies/privacy")
                        .isRequired(true)
                        .isAgreed(true)
                        .agreedAt(LocalDateTime.of(2025, 1, 1, 10, 0, 0))
                        .build(),
                UserPolicyAgreement.builder()
                        .userId(userId)
                        .type(PolicyType.MARKETING_CONSENT)
                        .title("마케팅 정보 수신 동의")
                        .url("https://domain.com/policies/marketing")
                        .isRequired(false)
                        .isAgreed(false)
                        .agreedAt(null)
                        .build()
        );
        return userPolicyAgreementRepository.saveAll(defaults);
    }
}