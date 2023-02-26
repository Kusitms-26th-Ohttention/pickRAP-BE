package pickRAP.server.repository.magazine;

import pickRAP.server.domain.magazine.Magazine;
import pickRAP.server.domain.member.Member;

import java.util.List;

public interface MagazineRepositoryCustom {
    List<Magazine> findMemberMagazines(String email);

    List<Magazine> findMagazineByHashtag(String keyword);

    List<Magazine> findMagazineByHashtagAndNotWriter(List<String> keyword, String email);

    List<Magazine> findMagazinesColorByMember(Member member);
}
