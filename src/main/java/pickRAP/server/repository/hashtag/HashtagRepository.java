package pickRAP.server.repository.hashtag;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pickRAP.server.domain.hashtag.Hashtag;
import pickRAP.server.domain.member.Member;

import java.util.List;

public interface HashtagRepository extends JpaRepository<Hashtag, Long>, HashtagRepositoryCustom {

    List<Hashtag> findByMember(Member member);

    @Query("select DISTINCT h.tag from Hashtag h where h.member = :member and h.profile = :profile order by h.tag")
    List<String> findTagDistinct(@Param("member") Member member, @Param("profile") boolean profile);

    @Query("select h from Hashtag h where h.member = :member and h.profile = true")
    List<Hashtag> findHashtagUseProfile(@Param("member") Member member);

    List<Hashtag> findByMemberAndTag(Member member, String tag);

}
