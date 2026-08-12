package com.umc.todayter.domain.mypage.repository;

import com.umc.todayter.domain.mypage.entity.UserPolicyAgreement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserPolicyAgreementRepository extends JpaRepository<UserPolicyAgreement, Long> {
    List<UserPolicyAgreement> findByUserId(Long userId);
}