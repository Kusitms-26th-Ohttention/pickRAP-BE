package pickRAP.server.repository.text;

import org.springframework.data.jpa.repository.JpaRepository;
import pickRAP.server.domain.member.Member;
import pickRAP.server.domain.text.Text;

import java.util.Optional;

public interface TextRepository extends JpaRepository<Text, Long>, TextRepositoryCustom {

    Optional<Text> findByWordAndMember(String word, Member member);
}
