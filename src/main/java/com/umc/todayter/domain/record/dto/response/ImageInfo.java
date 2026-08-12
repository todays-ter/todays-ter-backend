package com.umc.todayter.domain.record.dto.response;

import com.umc.todayter.domain.record.entity.VisitRecordImage;

import java.util.function.UnaryOperator;

public record ImageInfo(
        Long imageId,
        String imageUrl
) {
    public static ImageInfo from(VisitRecordImage image, UnaryOperator<String> urlResolver) {
        return new ImageInfo(image.getId(), urlResolver.apply(image.getImageUrl()));
    }
}
