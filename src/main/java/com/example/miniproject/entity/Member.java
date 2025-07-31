package com.example.miniproject.entity;

import com.example.miniproject.config.CustomOAuth2User;
import com.example.miniproject.config.CustomUserDetails;
import com.example.miniproject.constant.Role;
import com.example.miniproject.dto.MemberFormDto;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.crypto.password.PasswordEncoder;

@Entity
@Getter @Setter
@ToString
@NoArgsConstructor
@Table(name = "member", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"email"})})
public class Member extends BaseEntity {

    @Id
    @Column(name = "member_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String name;

    @Column(unique = true, nullable = false)
    private String nickname;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = true)
    private String password;

    @Column(nullable = true)
    private String postcode;

    @Column(nullable = true)
    private String address;

    @Column(nullable = true)
    private String detailAddress;

    @Column(nullable = true)
    private String extraAddress;

    @Enumerated(EnumType.STRING)
    Role role;

    @Column(length = 20)
    private String provider;

    @Column(nullable = false)
    private int point = 0;

    public static Member createMember(MemberFormDto memberFormDto, PasswordEncoder passwordEncoder) {
        Member member = new Member();
        member.setName(memberFormDto.getName());
        member.setNickname(memberFormDto.getNickname());
        member.setEmail(memberFormDto.getEmail());
        member.setPostcode(memberFormDto.getPostcode());
        member.setAddress(memberFormDto.getAddress());
        member.setDetailAddress(memberFormDto.getDetailAddress());
        member.setExtraAddress(memberFormDto.getExtraAddress());
        String password = passwordEncoder.encode(memberFormDto.getPassword());
        member.setPassword(password);
        member.setRole(Role.USER);
        member.setProvider("local");
        return member;
    }

    @Builder
    public Member(String email, String name, String nickname, String postcode,
                  String address, String detailAddress, String extraAddress,
                  String password, Role role, String provider) {
        this.email = email;
        this.name = name;
        this.nickname = nickname;
        this.postcode = postcode;
        this.address = address;
        this.detailAddress = detailAddress;
        this.extraAddress = extraAddress;
        this.password = password;
        this.role = role;
        this.provider = provider;
    }

    public void usePoint(int amount) {
        if (this.point < amount) {
            throw new IllegalStateException("보유 포인트가 부족합니다.");
        }
        this.point -= amount;
    }

    public void addPoint(int amount) {
        this.point += amount;
    }
}
