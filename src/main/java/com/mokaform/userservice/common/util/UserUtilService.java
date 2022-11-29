package com.mokaform.userservice.common.util;

import com.mokaform.userservice.common.exception.ApiException;
import com.mokaform.userservice.common.exception.errorcode.UserErrorCode;
import com.mokaform.userservice.domain.User;
import com.mokaform.userservice.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserUtilService {

    private final UserRepository userRepository;

    public UserUtilService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public User getUser(String email) {
        return userRepository.findByEmailAndIsWithdraw(email, false)
                .orElseThrow(() -> new ApiException(UserErrorCode.USER_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public void checkUser(String email) {
        if (!userRepository.existsByEmail(email)) {
            throw new ApiException(UserErrorCode.USER_NOT_FOUND);
        }
    }
}
