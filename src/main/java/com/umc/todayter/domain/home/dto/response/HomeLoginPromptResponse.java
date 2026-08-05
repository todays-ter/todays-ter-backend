package com.umc.todayter.domain.home.dto.response;

public record HomeLoginPromptResponse(
        String title,
        String buttonText
) {

    private static final String TITLE = "로그인 후 더 많은 터를 탐색해보세요";
    private static final String BUTTON_TEXT = "로그인/회원가입 하러가기";

    public static HomeLoginPromptResponse guest() {
        return new HomeLoginPromptResponse(TITLE, BUTTON_TEXT);
    }
}
