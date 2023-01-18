package pickRAP.server.repository.magazine;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pickRAP.server.domain.magazine.Magazine;
import pickRAP.server.domain.magazine.MagazinePage;

import java.util.List;

public interface MagazinePageRepository extends JpaRepository<MagazinePage, Long> {
    void deleteByMagazineId(Long magazine);

    @Query("select mp.text from MagazinePage mp where mp.id = :id")
    String findTextById(@Param("id") Long id);

    @Query("select mp.text from MagazinePage mp where mp.magazine = :magazine")
    List<String> findTextByMagazine(@Param("magazine") Magazine magazine);

    @Query("select mp.id from MagazinePage mp where mp.scrap.id = :scrapId")
    List<Long> findByScrapId(@Param("scrapId") Long scrapId);
}
