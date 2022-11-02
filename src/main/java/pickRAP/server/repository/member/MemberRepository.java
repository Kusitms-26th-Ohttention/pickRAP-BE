package pickRAP.server.repository.member;

import org.springframework.data.jpa.repository.JpaRepository;
import pickRAP.server.domain.member.Member;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmail(String email);

    boolean existsByEmail(String email);

}
