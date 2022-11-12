package pickRAP.server.domain.magazine;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pickRAP.server.domain.member.Member;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Magazine {
    @Id
    @GeneratedValue
    @Column(name = "magazine_id")
    private Long id;

    @Column(name = "title")
    private String title;

    @Column(name = "open_status")
    private boolean openStatus;

    @Enumerated(EnumType.STRING)
    private MagazineTemplate template;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @OneToMany(mappedBy = "magazine")
    private List<MagazinePage> pages = new ArrayList<>();

    @Builder
    public Magazine (String title, boolean openStatus, MagazineTemplate template) {
        this.title = title;
        this.openStatus = openStatus;
        this.template = template;
    }

    public void setMember(Member member) {
        this.member = member;
        member.getMagazines().add(this);
    }
}
