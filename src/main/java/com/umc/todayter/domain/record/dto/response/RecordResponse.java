package com.umc.todayter.domain.record.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.umc.todayter.domain.record.entity.VisitRecord;
import com.umc.todayter.domain.record.entity.VisitRecordImage;

import java.time.LocalDate;
import java.util.List;

public record RecordResponse(
        Long id,
        Long placeId,
        String placeName,

        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate visitedAt,

        String content,
        List<String> imageUrls
) {
    public static RecordResponse from(VisitRecord visitRecord, List<VisitRecordImage> images) {
        List<String> imageUrls = images.stream()
                .map(VisitRecordImage::getImageUrl)
                .toList();

        return new RecordResponse(
                visitRecord.getId(),
                visitRecord.getPlace().getId(),
                visitRecord.getPlace().getName(),
                visitRecord.getVisitedAt(),
                visitRecord.getContent(),
                imageUrls
        );
    }
}
