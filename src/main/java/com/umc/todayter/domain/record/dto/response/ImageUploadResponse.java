package com.umc.todayter.domain.record.dto.response;

import java.util.List;

public record ImageUploadResponse(
        List<ImageInfo> images
) {
}
