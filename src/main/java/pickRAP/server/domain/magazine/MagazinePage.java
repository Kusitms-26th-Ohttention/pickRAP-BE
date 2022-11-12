package pickRAP.server.domain.magazine;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MagazinePage {
    @Id
    @GeneratedValue
    @Column(name = "page_id")
    private Long id;

    @Column(name = "text", length = 1000)
    private String text;

    @Column(name = "page_order")
    private Long pageOrder;

    @ManyToOne
    @JoinColumn(name = "magazine_id")
    private Magazine magazine;

    //    @OneToOne
    //    private Scrap scrap;

    @Builder
    public MagazinePage (String text, Long pageOrder) {
        // this.scrap = scrap;
        this.text = text;
        this.pageOrder = pageOrder;
    }

    public void setMagazine(Magazine magazine) {
        this.magazine = magazine;
        magazine.getPages().add(this);
    }
}
