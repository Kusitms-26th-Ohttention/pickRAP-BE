package pickRAP.server.repository.magazine;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import pickRAP.server.domain.magazine.Magazine;
import pickRAP.server.domain.member.Member;

import java.util.List;

import static pickRAP.server.domain.hashtag.QHashtag.hashtag;
import static pickRAP.server.domain.magazine.QColor.color;
import static pickRAP.server.domain.magazine.QMagazine.magazine;
import static pickRAP.server.domain.magazine.QMagazinePage.magazinePage;
import static pickRAP.server.domain.member.QMember.member;
import static pickRAP.server.domain.scrap.QScrap.scrap;
import static pickRAP.server.domain.scrap.QScrapHashtag.scrapHashtag;

@RequiredArgsConstructor
public class MagazineRepositoryImpl implements MagazineRepositoryCustom{
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<Magazine> findMemberMagazines(String email) {
        return jpaQueryFactory
                .selectFrom(magazine)
                .join(magazine.member, member)
                .where(member.email.eq(email))
                .orderBy(magazine.createTime.desc())
                .fetch();
    }

    @Override
    public List<Magazine> findMagazineByHashtag(String keyword) {
        return jpaQueryFactory
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

    @Override
    public List<Magazine> findMagazineByHashtagAndNotWriter(List<String> keyword, String email) {
        BooleanBuilder builder = new BooleanBuilder();
        for(String k : keyword) {
            builder.and(hashtag.tag.contains(k));
        }
        builder.and(magazine.openStatus.eq(true));
        builder.and(magazine.member.email.ne(email));

        return jpaQueryFactory
                .selectFrom(magazine)
                .join(magazine.pages, magazinePage)
                .join(magazinePage.scrap, scrap)
                .join(scrap.scrapHashtags, scrapHashtag)
                .join(scrapHashtag.hashtag, hashtag)
                .where(builder)
                .distinct().fetch();
    }

    @Override
    public List<Magazine> findMagazinesColorByMember(Member member) {
        return jpaQueryFactory
                .select(magazine)
                .from(magazine)
                .innerJoin(color)
                .on(magazine.eq(color.magazine))
                .where(color.member.eq(member))
                .fetch();
    }
}
