package com.umc.todayter.domain.record.dto.response;

import com.umc.todayter.domain.record.entity.VisitRecord;
import com.umc.todayter.domain.record.entity.VisitRecordImage;

import java.time.LocalDateTime;
import java.util.List;

public record RecordResponse(
        Long recordId,
        Long placeId,
        String placeName,
        LocalDateTime visitVerifiedAt,
        Integer rating,
        String content,
        List<ImageInfo> images,
        LocalDateTime createdAt
) {
    public static RecordResponse from(VisitRecord visitRecord, List<VisitRecordImage> images, LocalDateTime visitVerifiedAt) {
        List<ImageInfo> imageInfos = images.stream()
                .map(ImageInfo::from)
                .toList();

        return new RecordResponse(
                visitRecord.getId(),
                visitRecord.getPlace().getId(),
                visitRecord.getPlace().getName(),
                visitVerifiedAt,
                visitRecord.getRating(),
                visitRecord.getContent(),
                imageInfos,
                visitRecord.getCreatedAt()
        );
    }
}
