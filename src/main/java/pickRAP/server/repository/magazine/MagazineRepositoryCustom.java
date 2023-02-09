package pickRAP.server.repository.magazine;

import pickRAP.server.domain.magazine.Magazine;

import java.util.List;

public interface MagazineRepositoryCustom {
    List<Magazine> findMemberMagazines(String email);
    List<Magazine> findMagazineByHashtag(String hashtag);
}
