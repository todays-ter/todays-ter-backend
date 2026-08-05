package com.umc.todayter.domain.place.dto.response;

import com.umc.todayter.domain.record.dto.response.ImageInfo;
import com.umc.todayter.domain.record.entity.VisitRecord;
import com.umc.todayter.domain.record.entity.VisitRecordImage;

import java.time.LocalDateTime;
import java.util.List;

public record PlaceReviewItemResponse(
        Long reviewId,
        String writerNickname,
        Integer rating,
        String content,
        List<ImageInfo> images,
        LocalDateTime createdAt
) {
    public static PlaceReviewItemResponse from(VisitRecord visitRecord, List<VisitRecordImage> images) {
        List<ImageInfo> imageInfos = images.stream()
                .map(ImageInfo::from)
                .toList();

        return new PlaceReviewItemResponse(
                visitRecord.getId(),
                visitRecord.getMember().getNickname(),
                visitRecord.getRating(),
                visitRecord.getContent(),
                imageInfos,
                visitRecord.getCreatedAt()
        );
    }
}
