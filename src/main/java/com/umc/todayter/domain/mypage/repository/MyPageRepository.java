package com.umc.todayter.domain.mypage.repository;

import com.umc.todayter.domain.mypage.entity.MyPage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MyPageRepository extends JpaRepository<MyPage, Long> {
}