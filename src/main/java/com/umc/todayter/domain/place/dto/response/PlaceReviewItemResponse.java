package com.umc.todayter.domain.place.dto.response;

import com.umc.todayter.domain.record.entity.VisitRecord;
import com.umc.todayter.domain.record.entity.VisitRecordImage;

import java.time.LocalDateTime;
import java.util.List;

public record PlaceReviewItemResponse(
        Long reviewId,
        String memberNickname,
        Integer rating,
        String content,
        List<String> imageUrls,
        LocalDateTime createdAt
) {
    public static PlaceReviewItemResponse from(VisitRecord visitRecord, List<VisitRecordImage> images) {
        List<String> imageUrls = images.stream()
                .map(VisitRecordImage::getImageUrl)
                .toList();

        return new PlaceReviewItemResponse(
                visitRecord.getId(),
                visitRecord.getMember().getNickname(),
                visitRecord.getRating(),
                visitRecord.getContent(),
                imageUrls,
                visitRecord.getCreatedAt()
        );
    }
}
