package com.umc.todayter.domain.record.dto.response;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.umc.todayter.domain.record.entity.VisitRecord;
import com.umc.todayter.domain.record.entity.VisitRecordImage;
import com.umc.todayter.domain.record.enums.RecordType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

public record RecordDetailResponse(
        @JsonIgnore Long id,
        @JsonIgnore RecordType type,
        Long placeId,
        String placeName,
        LocalDateTime visitVerifiedAt,
        Integer rating,
        String content,
        List<String> imageUrls,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    // 명세상 id 필드명이 type에 따라 recordId/reviewId로 달라져야 해서, 고정 필드 대신 동적 키로 내려줌
    @JsonAnyGetter
    public Map<String, Object> idField() {
        String key = type == RecordType.REVIEW ? "reviewId" : "recordId";
        return Map.of(key, id);
    }

    public static RecordDetailResponse from(
            VisitRecord visitRecord,
            List<VisitRecordImage> images,
            LocalDateTime visitVerifiedAt,
            UnaryOperator<String> urlResolver
    ) {
        List<String> imageUrls = images.stream()
                .map(VisitRecordImage::getImageUrl)
                .map(urlResolver)
                .toList();

        return new RecordDetailResponse(
                visitRecord.getId(),
                visitRecord.getType(),
                visitRecord.getPlace().getId(),
                visitRecord.getPlace().getName(),
                visitVerifiedAt,
                visitRecord.getRating(),
                visitRecord.getContent(),
                imageUrls,
                visitRecord.getCreatedAt(),
                visitRecord.getUpdatedAt()
        );
    }
}
