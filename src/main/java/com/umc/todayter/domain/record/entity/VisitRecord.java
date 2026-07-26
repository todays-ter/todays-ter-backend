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

    // REVIEW 타입에 한해 memberId:placeId 값을 채워 unique 제약으로 동시 중복 작성을 막는다.
    // RECORD 타입은 null로 두어(MySQL unique는 null 중복을 허용) 여러 번 작성 가능하게 한다.
    @Column(name = "review_uniqueness_key", unique = true)
    private String reviewUniquenessKey;

    private VisitRecord(Member member, Place place, RecordType type, Integer rating, String content) {
        this.member = member;
        this.place = place;
        this.type = type;
        this.rating = rating;
        this.content = content;
        this.reviewUniquenessKey = type == RecordType.REVIEW
                ? member.getId() + ":" + place.getId()
                : null;
    }

    public static VisitRecord create(Member member, Place place, RecordType type, Integer rating, String content) {
        return new VisitRecord(member, place, type, rating, content);
    }

    public void updateRatingAndContent(Integer rating, String content) {
        if (rating != null) {
            this.rating = rating;
        }
        if (content != null) {
            this.content = content;
        }
    }
}
