package pickRAP.server.repository.magazine;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import pickRAP.server.domain.category.Category;
import pickRAP.server.domain.magazine.Magazine;

import java.util.List;

import static pickRAP.server.domain.hashtag.QHashtag.hashtag;
import static pickRAP.server.domain.magazine.QMagazine.magazine;
import static pickRAP.server.domain.magazine.QMagazinePage.magazinePage;
import static pickRAP.server.domain.member.QMember.member;
import static pickRAP.server.domain.scrap.QScrap.scrap;
import static pickRAP.server.domain.scrap.QScrapHashtag.scrapHashtag;

@RequiredArgsConstructor
public class MagazineRepositoryImpl implements MagazineRepositoryCustom{
    private final JPAQueryFactory queryFactory;

    @Override
    public List<Magazine> findMemberMagazines(String email) {
        return queryFactory
                .selectFrom(magazine)
                .join(magazine.member, member)
                .where(member.email.eq(email))
                .orderBy(magazine.createTime.desc())
                .fetch();
    }

    @Override
    public List<Magazine> findMagazineByHashtag(String keyword) {
        return queryFactory
                .selectFrom(magazine)
                .join(magazine.pages, magazinePage)
                .join(magazinePage.scrap, scrap)
                .join(scrap.scrapHashtags, scrapHashtag)
                .join(scrapHashtag.hashtag, hashtag)
                .where(
                        hashtag.tag.contains(keyword),
                        magazine.openStatus.eq(true)
                )
                .distinct().fetch();
    }
}
