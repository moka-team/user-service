package com.mokaform.userservice.domain;

import com.mokaform.userservice.common.entitiy.BaseEntity;
import com.mokaform.userservice.domain.enums.Category;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "preference_category")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class PreferenceCategory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "preference_category_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    private User user;

    @Column(name = "category_name", nullable = false, length = 20)
    @Enumerated(value = EnumType.STRING)
    private Category category;

    public PreferenceCategory(User user, Category category) {
        this.user = user;
        this.category = category;
    }
}
