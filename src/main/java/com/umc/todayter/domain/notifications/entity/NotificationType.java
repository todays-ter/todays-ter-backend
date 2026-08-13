package com.umc.todayter.domain.notifications.entity;

public enum NotificationType {
    RECOMMENDATION,     // 회원의 첫 추천터 도착
    TODAY_REMIND,       // 오늘의 추천터 확인 리마인드
    SAVED_PLACE,        // 저장한 터 중 오늘 가장 잘 맞는 터
    VISIT_RECORD_REMIND,// 추천터 확인/저장 후 방문 기록 리마인드
    SERVICE_NOTICE,     // 서비스 중요 공지
    MARKETING,          // 마케팅 정보 수신
    CREW,               // 크루 알림

    // 기존 DB 알림과의 호환성을 위한 예전 값
    REMIND,
    NOTICE
}
