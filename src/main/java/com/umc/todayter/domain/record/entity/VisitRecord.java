package com.umc.todayter.domain.record.entity;

import com.umc.todayter.domain.member.entity.Member;
import com.umc.todayter.domain.place.entity.Place;
import com.umc.todayter.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "visit_records")
public class VisitRecord extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "member_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_visit_records_member")
    )
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "place_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_visit_records_place")
    )
    private Place place;

    @Column(nullable = false, columnDefinition = "text")
    private String content;

    @Column(name = "visited_at", nullable = false)
    private LocalDate visitedAt;

    private VisitRecord(Member member, Place place, String content, LocalDate visitedAt) {
        this.member = member;
        this.place = place;
        this.content = content;
        this.visitedAt = visitedAt;
    }

    public static VisitRecord create(Member member, Place place, String content, LocalDate visitedAt) {
        return new VisitRecord(member, place, content, visitedAt);
    }
}
