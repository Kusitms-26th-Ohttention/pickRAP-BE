package pickRAP.server.domain;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity{

    @Id
    @GeneratedValue
    @Column(name = "member_id")
    private Long id;

    private String email;

    @Column(name = "email_kakao")
    private String kakaoEmail;

    @Column(name = "email_google")
    private String googleEmail;

    private String password;

    private String name;

    @Enumerated(EnumType.STRING)
    private Authority authority;

    @Enumerated(EnumType.STRING)
    private SocialType socialType;

    @Builder
    public Member(String email, String kakaoEmail, String googleEmail, SocialType socialType, String password, String name) {
        this.email = email;
        this.kakaoEmail = kakaoEmail;
        this.googleEmail = googleEmail;
        this.password = password;
        this.name = name;
        this.authority = Authority.ROLE_USER;
    }
}
