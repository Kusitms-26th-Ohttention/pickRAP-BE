package pickRAP.server.repository.word;

import org.springframework.data.jpa.repository.JpaRepository;
import pickRAP.server.domain.member.Member;
import pickRAP.server.domain.word.Word;

import java.util.List;
import java.util.Optional;

public interface WordRepository extends JpaRepository<Word, Long>, WordRepositoryCustom {

    Optional<Word> findByValueAndMember(String value, Member member);

    List<Word> findTop5ByMemberOrderByCountDesc(Member member);
}
