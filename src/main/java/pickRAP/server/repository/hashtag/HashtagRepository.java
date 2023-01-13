package pickRAP.server.repository.scrap;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pickRAP.server.domain.member.Member;
import pickRAP.server.domain.scrap.Hashtag;

import java.util.Optional;

public interface HashtagRepository extends JpaRepository<Hashtag, Long>, HashTagRepositoryCustom{

    @Query("select h from Hashtag h where h.tag = :tag and h.member = :member")
    Optional<Hashtag> findMemberHashtag(@Param("tag") String tag, @Param("member")Member member);
}
