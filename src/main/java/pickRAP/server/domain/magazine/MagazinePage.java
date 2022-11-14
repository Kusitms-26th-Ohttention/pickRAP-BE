package pickRAP.server.domain.magazine;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MagazinePage {
    @Id
    @GeneratedValue
    @Column(name = "page_id")
    private Long id;

    @Column(name = "text", length = 400)
    private String text;

    @ManyToOne
    @JoinColumn(name = "magazine_id")
    private Magazine magazine;

    //    @OneToOne
    //    private Scrap scrap;

    @Builder
    public MagazinePage (String text) {
        // this.scrap = scrap;
        this.text = text;
    }

    public void setMagazine(Magazine magazine) {
        this.magazine = magazine;
        magazine.getPages().add(this);
    }

}
