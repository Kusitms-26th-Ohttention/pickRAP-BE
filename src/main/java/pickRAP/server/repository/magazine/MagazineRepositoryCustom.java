package pickRAP.server.repository.magazine;

import pickRAP.server.domain.magazine.Magazine;
import pickRAP.server.domain.member.Member;

import java.util.List;

public interface MagazineRepositoryCustom {
    List<Magazine> findMemberMagazines(String email);

}
