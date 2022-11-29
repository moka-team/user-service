package com.mokaform.userservice.service;

import com.mokaform.userservice.common.exception.ApiException;
import com.mokaform.userservice.common.exception.errorcode.CommonErrorCode;
import com.mokaform.userservice.common.exception.errorcode.UserErrorCode;
import com.mokaform.userservice.domain.PreferenceCategory;
import com.mokaform.userservice.domain.Role;
import com.mokaform.userservice.domain.User;
import com.mokaform.userservice.domain.enums.*;
import com.mokaform.userservice.dto.request.ResetPasswordRequest;
import com.mokaform.userservice.dto.request.SignupRequest;
import com.mokaform.userservice.dto.response.DuplicateValidationResponse;
import com.mokaform.userservice.dto.response.UserGetResponse;
import com.mokaform.userservice.repository.PreferenceCategoryRepository;
import com.mokaform.userservice.repository.RoleRepository;
import com.mokaform.userservice.repository.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.List;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PreferenceCategoryRepository preferenceCategoryRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       PreferenceCategoryRepository preferenceCategoryRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.preferenceCategoryRepository = preferenceCategoryRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void createUser(SignupRequest request) {
        Role userRole = roleRepository.findByName(RoleName.USER)
                .orElseThrow(() -> new ApiException(CommonErrorCode.RESOURCE_NOT_FOUND));

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname())
                .ageGroup(AgeGroup.valueOf(request.getAgeGroup()))
                .gender(Gender.valueOf(request.getGender()))
                .job(Job.valueOf(request.getJob()))
                .roles(List.of(userRole))
                .build();
        userRepository.save(user);

        request.getCategory()
                .forEach(v -> createPreferenceCategory(user, v));
    }

    @Transactional(readOnly = true)
    public DuplicateValidationResponse checkEmailDuplication(String email) {
        Boolean isDuplicated = userRepository.existsByEmail(email);
        return new DuplicateValidationResponse(isDuplicated);
    }

    @Transactional(readOnly = true)
    public DuplicateValidationResponse checkNicknameDuplication(String nickname) {
        Boolean isDuplicated = userRepository.existsByNickname(nickname);
        return new DuplicateValidationResponse(isDuplicated);
    }

    private void createPreferenceCategory(User user, String categoryValue) {
        PreferenceCategory preferenceCategory = new PreferenceCategory(user, Category.valueOf(categoryValue));
        preferenceCategoryRepository.save(preferenceCategory);
    }

    @Transactional(readOnly = true)
    public User login(String principal, String credentials) {
        Assert.isTrue(isNotEmpty(principal), "principal must be provided.");
        Assert.isTrue(isNotEmpty(credentials), "credentials must be provided.");

        User user = userRepository.findByEmailAndIsWithdraw(principal, false)
                .orElseThrow(() -> new UsernameNotFoundException("Could not found user for " + principal));
        user.checkPassword(passwordEncoder, credentials);
        return user;
    }

    @Transactional(readOnly = true)
    public UserGetResponse getUserInfo(String userEmail) {
        User user = userRepository.findByEmailAndIsWithdraw(userEmail, false)
                .orElseThrow(() -> new ApiException(UserErrorCode.USER_NOT_FOUND));
        List<PreferenceCategory> preferenceCategories = preferenceCategoryRepository.findByUserId(user.getId());

        return new UserGetResponse(user, preferenceCategories);
    }

    @Transactional
    public void updatePassword(ResetPasswordRequest request) {
        User user = userRepository.findByEmailAndIsWithdraw(request.getEmail(), false)
                .orElseThrow(() -> new ApiException(UserErrorCode.USER_NOT_FOUND));
        user.updatePassword(passwordEncoder.encode(request.getPassword()));
    }

    @Transactional
    public void withdraw(String userEmail) {
        User user = userRepository.findByEmailAndIsWithdraw(userEmail, false)
                .orElseThrow(() -> new ApiException(UserErrorCode.USER_NOT_FOUND));
        user.withdraw();
//TODO: 탈퇴 회원 생성 설문 삭제 API 구현
//        List<Survey> surveys = surveyRepository.findAllByUser_Id(user.getId());
//        surveys.forEach(s -> s.updateIsDeleted(true));
    }
}
