package com.umc.todayter.global.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

public record ShareLinkResponse(
        @JsonInclude(JsonInclude.Include.NON_NULL) String shareToken,
        String shareUrl
) {
    public static ShareLinkResponse forFortuneReport(String shareToken, String shareUrl) {
        return new ShareLinkResponse(shareToken, shareUrl);
    }

    public static ShareLinkResponse forPlace(String shareToken, String shareUrl) {
        return new ShareLinkResponse(shareToken, shareUrl);
    }
}
