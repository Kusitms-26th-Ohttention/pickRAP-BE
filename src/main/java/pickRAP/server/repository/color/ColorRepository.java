package pickRAP.server.repository.color;

import org.springframework.data.jpa.repository.JpaRepository;
import pickRAP.server.domain.magazine.Color;
import pickRAP.server.domain.magazine.Magazine;
import pickRAP.server.domain.member.Member;

import java.util.Optional;

public interface ColorRepository extends JpaRepository<Color, Long>, ColorRepositoryCustom {

    Optional<Color> findByMemberAndMagazine(Member member, Magazine magazine);

}
