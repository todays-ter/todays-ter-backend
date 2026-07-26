package com.umc.todayter.domain.record.entity;

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

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "visit_record_images")
public class VisitRecordImage extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "visit_record_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_visit_record_images_visit_record")
    )
    private VisitRecord visitRecord;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    private VisitRecordImage(VisitRecord visitRecord, String imageUrl, Integer sortOrder) {
        this.visitRecord = visitRecord;
        this.imageUrl = imageUrl;
        this.sortOrder = sortOrder;
    }

    public static VisitRecordImage create(VisitRecord visitRecord, String imageUrl, Integer sortOrder) {
        return new VisitRecordImage(visitRecord, imageUrl, sortOrder);
    }
}
