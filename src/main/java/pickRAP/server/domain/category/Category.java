package pickRAP.server.domain.category;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pickRAP.server.common.BaseEntity;
import pickRAP.server.domain.member.Member;
import pickRAP.server.domain.scrap.Scrap;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category extends BaseEntity {

    @Id
    @GeneratedValue
    @Column(name = "category_id")
    private Long id;

    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @OneToMany(mappedBy = "category")
    private List<Scrap> scraps = new ArrayList<>();

    @Builder
    public Category(String name) {
        this.name = name;
    }

    public void setMember(Member member) {
        this.member = member;
        member.getCategories().add(this);
    }

    public void updateName(String name) {
        this.name = name;
    }
}
