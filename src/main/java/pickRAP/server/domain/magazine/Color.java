package pickRAP.server.domain.magazine;

import lombok.*;
import pickRAP.server.common.BaseEntity;
import pickRAP.server.domain.member.Member;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Color extends BaseEntity {

    @Id
    @GeneratedValue
    @Column(name = "color_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    private ColorType colorType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "magazine_id")
    private Magazine magazine;

    private void setMagazine(Magazine magazine) {
        this.magazine = magazine;
        magazine.getColors().add(this);
    }

    private void setMember(Member member) {
        this.member = member;
        member.getColors().add(this);
    }

    @Builder
    public Color(ColorType colorType, Magazine magazine, Member member) {
        this.colorType = colorType;
        setMagazine(magazine);
        setMember(member);
    }

    public void updateColor(ColorType colorType) {
        this.colorType = colorType;
    }

}
