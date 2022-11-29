package com.mokaform.userservice.domain.enums;


import com.mokaform.userservice.common.exception.ApiException;
import com.mokaform.userservice.common.exception.errorcode.SurveyErrorCode;

import java.util.Arrays;

public enum Category {
    DAILY_LIFE,             // 일상
    IT,                     // IT
    HOBBY,                  // 취미
    LEARNING,               // 학습
    PSYCHOLOGY,             // 취미
    SOCIAL_POLITICS,        // 사회/정치
    PREFERENCE_RESEARCH,     // 선호도 조사
    PET;                     // 반려동물

    public static Category getCategory(String categoryName) {
        return Arrays.stream(Category.values())
                .filter(type -> type.name().equals(categoryName))
                .findAny()
                .orElseThrow(() -> new ApiException(SurveyErrorCode.INVALID_CATEGORY_TYPE));
    }
}
