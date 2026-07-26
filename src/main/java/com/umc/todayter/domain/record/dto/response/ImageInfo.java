package com.umc.todayter.domain.record.dto.response;

import com.umc.todayter.domain.record.entity.VisitRecordImage;

public record ImageInfo(
        Long imageId,
        String imageUrl
) {
    public static ImageInfo from(VisitRecordImage image) {
        return new ImageInfo(image.getId(), image.getImageUrl());
    }
}
