package pickRAP.server.domain.magazine;

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
public class Magazine extends BaseEntity {
    @Id
    @GeneratedValue
    @Column(name = "magazine_id")
    private Long id;

    @Column(name = "title", length = 40)
    private String title;

    @Column(name = "open_status")
    private boolean openStatus;

    @Column(name = "cover")
    private String cover;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @OneToMany(mappedBy = "magazine", cascade = CascadeType.ALL)
    private List<MagazinePage> pages = new ArrayList<>();

    @Builder
    public Magazine (String title, boolean openStatus, Member member, String cover) {
        this.title = title;
        this.openStatus = openStatus;
        this.member = member;
        member.getMagazines().add(this);
        this.cover = cover;
    }

    public void updateTitle(String title) {
        if(!this.title.equals(title)) {
            this.title = title;
        }
    }

    public void updateOpenStatus() {
        this.openStatus = !this.openStatus;
    }

    public boolean checkWriter(String email) {
        if(email.equals(this.member.getEmail())) {
            return true;
        } else {
            return false;
        }
    }
}
