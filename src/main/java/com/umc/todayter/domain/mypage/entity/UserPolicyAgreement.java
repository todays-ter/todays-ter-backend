package com.umc.todayter.domain.mypage.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class UserPolicyAgreement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @Enumerated(EnumType.STRING)
    private PolicyType type;

    private String title;

    private String url;

    private boolean isRequired;

    private boolean isAgreed;

    private LocalDateTime agreedAt;
}