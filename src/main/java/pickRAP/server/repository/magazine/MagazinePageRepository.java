package pickRAP.server.repository.magazine;

import org.springframework.data.jpa.repository.JpaRepository;
import pickRAP.server.domain.magazine.Magazine;
import pickRAP.server.domain.magazine.MagazinePage;

public interface MagazinePageRepository extends JpaRepository<MagazinePage, Long> {
    void deleteByMagazine(Magazine magazine);
}
