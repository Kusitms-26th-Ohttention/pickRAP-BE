package pickRAP.server.repository.member;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pickRAP.server.domain.member.Member;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("select count(m) from Member m where m <> :member and m.nickname = :nickname")
    int getNicknameCount(@Param("member") Member member, @Param("nickname") String nickname);
}
