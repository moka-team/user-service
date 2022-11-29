package com.mokaform.userservice.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.mokaform.userservice.domain.PreferenceCategory;
import com.mokaform.userservice.domain.User;
import com.mokaform.userservice.domain.enums.AgeGroup;
import com.mokaform.userservice.domain.enums.Category;
import com.mokaform.userservice.domain.enums.Gender;
import com.mokaform.userservice.domain.enums.Job;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@JsonInclude(NON_NULL)
@Getter
public class UserGetResponse {

    private final Long userId;

    private final String email;

    private final String nickname;

    private final AgeGroup ageGroup;

    private final Gender gender;

    private final Job job;

    private final String profileImage;

    private final List<Category> categories;

    public UserGetResponse(User user, List<PreferenceCategory> categories) {
        this.userId = user.getId();
        this.email = user.getEmail();
        this.nickname = user.getNickname();
        this.ageGroup = user.getAgeGroup();
        this.gender = user.getGender();
        this.job = user.getJob();
        this.profileImage = user.getProfileImage();
        this.categories = categories.stream()
                .map(PreferenceCategory::getCategory)
                .collect(Collectors.toList());
    }
}
