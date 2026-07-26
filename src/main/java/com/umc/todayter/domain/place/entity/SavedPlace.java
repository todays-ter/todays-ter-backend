package com.umc.todayter.domain.place.entity;

import com.umc.todayter.domain.member.entity.Member;
import com.umc.todayter.global.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 장소 저장(북마크) 엔티티. 저장/취소 토글 API는 아직 별도로 구현 예정(다른 팀원 담당)이며,
// 이 이슈(#28)에서는 목록 조회에 필요한 최소 스펙(member, place, 저장일)만 정의한다.
// 저장/취소 기능을 구현할 때는 새 엔티티를 만들지 말고 이 엔티티를 재사용/조율해주세요.
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "saved_places",
        uniqueConstraints = @UniqueConstraint(name = "uk_saved_places_member_place", columnNames = {"member_id", "place_id"})
)
public class SavedPlace extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "member_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_saved_places_member")
    )
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "place_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_saved_places_place")
    )
    private Place place;

    @Builder
    private SavedPlace(Member member, Place place) {
        this.member = member;
        this.place = place;
    }
}
