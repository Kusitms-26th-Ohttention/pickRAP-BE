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

    private int count;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

//    @OneToMany(mappedBy = "hashtag")
//    private List<ScrapHashtag> scrapHashtags = new ArrayList<>();

    @Builder
    public Hashtag(String tag, Member member) {
        this.tag = tag;
        this.count = 1;
        setMember(member);
    }

    public void setMember(Member member) {
        this.member = member;
        member.getHashtags().add(this);
    }

    public void plusCount() {
        count++;
    }

    public void minusCount() {
        count--;
    }
}
