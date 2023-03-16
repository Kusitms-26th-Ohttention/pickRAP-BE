package pickRAP.server.domain.member;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pickRAP.server.common.BaseEntity;
import pickRAP.server.domain.category.Category;
import pickRAP.server.domain.hashtag.Hashtag;
import pickRAP.server.domain.magazine.Color;
import pickRAP.server.domain.magazine.Magazine;
import pickRAP.server.domain.scrap.Scrap;
import pickRAP.server.domain.text.Text;
import pickRAP.server.service.auth.DefaultProfileEnv;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

import static pickRAP.server.domain.member.Authority.ROLE_USER;
import static pickRAP.server.service.auth.DefaultProfileEnv.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity {

    @Id
    @GeneratedValue
    @Column(name = "member_id")
    private Long id;

    private String email;

    private String password;

    private String name;

    private String profileImageUrl;

    private String introduction;

    private String nickname;

    @Enumerated(EnumType.STRING)
    private Authority authority;

    @Enumerated(EnumType.STRING)
    private SocialType socialType;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<Category> categories = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<Magazine> magazines = new ArrayList<>();

    @OneToMany(mappedBy = "member")
    private List<Scrap> scraps = new ArrayList<>();

    @OneToMany(mappedBy = "member")
    private List<Hashtag> hashtags = new ArrayList<>();

    @OneToMany(mappedBy = "member")
    private List<Text> texts = new ArrayList<>();

    @OneToMany(mappedBy = "member")
    private List<Color> colors = new ArrayList<>();

    @Builder
    public Member(String email, SocialType socialType, String password, String name) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.profileImageUrl = DEFAULT_IMAGE_URL;
        this.introduction = DEFAULT_INTRODUCTION;
        this.nickname = DEFAULT_NICKNAME;
        this.authority = ROLE_USER;
        this.socialType = socialType;
    }

    public void updateProfile(String nickname, String introduction, String profileImageUrl) {
        this.nickname = nickname;
        this.introduction = introduction;
        this.profileImageUrl = profileImageUrl;
    }
}
