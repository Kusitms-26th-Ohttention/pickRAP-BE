package pickRAP.server.repository.magazine;

import org.springframework.data.jpa.repository.JpaRepository;
import pickRAP.server.domain.magazine.Magazine;
import pickRAP.server.domain.member.Member;

import java.util.Optional;


public interface MagazineRepository extends JpaRepository<Magazine, Long> {
    Optional<Magazine> findByTitleAndMember(String title, Member member);
}
