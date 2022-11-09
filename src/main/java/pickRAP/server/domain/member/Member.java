package pickRAP.server.domain.member;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pickRAP.server.common.BaseEntity;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity {

    @Id
    @GeneratedValue
    @Column(name = "member_id")
    private Long id;

    private String email;

    private String kakaoEmail;

    private String naverEmail;

    private String password;

    private String name;

    private String description;

    private String imageUrl;

    private String keyword;

    @Enumerated(EnumType.STRING)
    private Authority authority;

    @Enumerated(EnumType.STRING)
    private SocialType socialType;

    @Builder
    public Member(String email, String kakaoEmail, String naverEmail, SocialType socialType, String password, String name) {
        this.email = email;
        this.kakaoEmail = kakaoEmail;
        this.naverEmail = naverEmail;
        this.password = password;
        this.name = name;
        this.authority = Authority.ROLE_USER;
        this.socialType = socialType;
    }

    public void updateProfile(String name, String description, String imageUrl, String keyword) {
        if(name != null) {
            this.name = name;
        }
        if(description != null) {
            this.description = description;
        }
        if(!imageUrl.equals("")) {
            this.imageUrl = imageUrl;
        }
        if(keyword != null) {
            this.keyword = keyword;
        }
    }
}
