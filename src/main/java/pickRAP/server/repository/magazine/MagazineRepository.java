package pickRAP.server.repository.magazine;

import org.springframework.data.jpa.repository.JpaRepository;
import pickRAP.server.domain.magazine.Magazine;

public interface MagazineRepository extends JpaRepository<Magazine, Long> {
}
