package com.umc.todayter.domain.place.dto.response;

import java.util.List;

public record EditorPickResponse(
        List<EditorPickItemResponse> content
) {
}
