package com.mokaform.userservice.repository;

import com.mokaform.userservice.domain.PreferenceCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PreferenceCategoryRepository extends JpaRepository<PreferenceCategory, Long> {

    List<PreferenceCategory> findByUserId(Long userId);
}
