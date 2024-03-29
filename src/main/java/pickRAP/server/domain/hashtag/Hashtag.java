package pickRAP.server.domain.hashtag;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pickRAP.server.common.BaseEntity;
import pickRAP.server.common.BooleanToYNConverter;
import pickRAP.server.domain.member.Member;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Hashtag extends BaseEntity {

    @Id
    @GeneratedValue
    @Column(name = "hashtag_id")
    private Long id;

    private String tag;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Convert(converter = BooleanToYNConverter.class)
    private boolean usedInProfile;

    @Builder
    public Hashtag(String tag, Member member) {
        this.tag = tag;
        this.usedInProfile = false;
        setMember(member);
    }

    private void setMember(Member member) {
        this.member = member;
        member.getHashtags().add(this);
    }

    public void updateProfile(boolean usedInProfile) {
        this.usedInProfile = usedInProfile;
    }
}
