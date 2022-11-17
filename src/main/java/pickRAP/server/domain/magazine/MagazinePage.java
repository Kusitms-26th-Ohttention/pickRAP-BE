package pickRAP.server.domain.magazine;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pickRAP.server.domain.scrap.Scrap;

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

    @OneToOne
    @JoinColumn(name = "scrap_id")
    private Scrap scrap;

    @Builder
    public MagazinePage (Scrap scrap, String text, Magazine magazine) {
        this.scrap = scrap;
        this.text = text;
        this.magazine = magazine;
        magazine.getPages().add(this);
    }
}
