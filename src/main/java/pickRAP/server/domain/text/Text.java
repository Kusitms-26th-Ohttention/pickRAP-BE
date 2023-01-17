package pickRAP.server.domain.text;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pickRAP.server.common.BaseEntity;
import pickRAP.server.domain.member.Member;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Text extends BaseEntity {

    @Id
    @GeneratedValue
    @Column(name = "text_id")
    private Long id;

    private String word;

    private long count;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Builder
    public Text(String word, long count, Member member) {
        this.word = word;
        this.count = count;
        setMember(member);
    }

    public void setMember(Member member) {
        this.member = member;
        member.getTexts().add(this);
    }

    public void plusCount(long cnt) {
        count = count + cnt;
    }

    public void minusCount(long cnt) {
        count = count - cnt;
    }
}
