package pickRAP.server.domain.scrap;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pickRAP.server.common.BaseEntity;
import pickRAP.server.domain.member.Member;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

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

    @Builder
    public Hashtag(String tag, Member member) {
        this.tag = tag;
        setMember(member);
    }

    public void setMember(Member member) {
        this.member = member;
        member.getHashtags().add(this);
    }

}
