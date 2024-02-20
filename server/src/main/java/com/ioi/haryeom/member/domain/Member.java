package com.ioi.haryeom.member.domain;

import com.ioi.haryeom.common.domain.BaseTimeEntity;
import com.ioi.haryeom.member.domain.type.MemberStatus;
import com.ioi.haryeom.member.domain.type.Role;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Member extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private Role role = Role.GUEST;

    @Enumerated(EnumType.STRING)
    private MemberStatus status = MemberStatus.ACTIVATED;

    private String oauthId;

    private String profileUrl;

    private String name;

    private String phone;

    @Builder
    public Member(Long id, Role role, MemberStatus status,
        String oauthId, String profileUrl, String name, String phone) {
        this.id = id;
        this.role = role;
        this.status = status;
        this.oauthId = oauthId;
        this.profileUrl = profileUrl;
        this.name = name;
        this.phone = phone;
    }

    public void create(Role role, String profileUrl, String name,
        String phone) {
        this.role = role;
        this.profileUrl = profileUrl;
        this.name = name;
        this.phone = phone;
    }

    public void update(String profileUrl, String name,
        String phone) {
        this.profileUrl = profileUrl;
        this.name = name;
        this.phone = phone;
    }


    public void delete() {
        this.status = MemberStatus.DELETED;
    }
}

