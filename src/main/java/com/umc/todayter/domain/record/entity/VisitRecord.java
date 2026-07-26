package com.umc.todayter.domain.record.entity;

import com.umc.todayter.domain.member.entity.Member;
import com.umc.todayter.domain.place.entity.Place;
import com.umc.todayter.domain.record.enums.RecordType;
import com.umc.todayter.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RecordType type;

    @Column(nullable = false)
    private Integer rating;

    @Column(nullable = false, columnDefinition = "text")
    private String content;

    private VisitRecord(Member member, Place place, RecordType type, Integer rating, String content) {
        this.member = member;
        this.place = place;
        this.type = type;
        this.rating = rating;
        this.content = content;
    }

    public static VisitRecord create(Member member, Place place, RecordType type, Integer rating, String content) {
        return new VisitRecord(member, place, type, rating, content);
    }
}
