package pickRAP.server.domain.member;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pickRAP.server.common.BaseEntity;
import pickRAP.server.domain.category.Category;

import javax.persistence.*;
import java.util.List;

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

    private String keyword;

    @Enumerated(EnumType.STRING)
    private Authority authority;

    @Enumerated(EnumType.STRING)
    private SocialType socialType;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<Category> categories;

    @Builder
    public Member(String email, SocialType socialType, String password, String name, String profileImageUrl) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.profileImageUrl = profileImageUrl;
        this.authority = Authority.ROLE_USER;
        this.socialType = socialType;
    }

    public void updateProfile(String name, String introduction, String profileImageUrl, String keyword) {
        if(name != null) {
            this.name = name;
        }
        if(introduction != null) {
            this.introduction = introduction;
        }
        if(!profileImageUrl.equals("")) {
            this.profileImageUrl = profileImageUrl;
        }
        if(keyword != null) {
            this.keyword = keyword;
        }
    }
}
