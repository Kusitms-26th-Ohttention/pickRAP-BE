package pickRAP.server.domain.scrap;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pickRAP.server.common.BaseEntity;
import pickRAP.server.domain.category.Category;
import pickRAP.server.domain.magazine.MagazinePage;
import pickRAP.server.domain.member.Member;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Scrap extends BaseEntity {

    @Id
    @GeneratedValue
    @Column(name = "scrap_id")
    private Long id;

    private String title;

    private String content;

    private String memo;

    private String fileUrl;

    private LocalDateTime revisitTime;

    private Long revisitCount;

    private String previewUrl;

    @Enumerated(EnumType.STRING)
    private ScrapType scrapType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @OneToMany(mappedBy = "scrap")
    private List<ScrapHashtag> scrapHashtags = new ArrayList<>();

    @Builder
    public Scrap(String title, String content, String memo, String fileUrl, ScrapType scrapType,
                 LocalDateTime revisitTime, Long revisitCount, String previewUrl) {
        this.title = title;
        this.content = content;
        this.memo = memo;
        this.fileUrl = fileUrl;
        this.scrapType = scrapType;
        this.revisitTime = revisitTime;
        this.revisitCount = revisitCount;
        this.previewUrl = previewUrl;
    }

    public void setMember(Member member) {
        this.member = member;
        member.getScraps().add(this);
    }

    public void setCategory(Category category) {
        this.category = category;
        category.getScraps().add(this);
    }

    public void updateScrap(String title, String memo) {
        this.title = title;
        this.memo = memo;
    }

    public void updateRevisitRecord() {
        this.revisitCount++;
        this.revisitTime = LocalDateTime.now();
    }
}
