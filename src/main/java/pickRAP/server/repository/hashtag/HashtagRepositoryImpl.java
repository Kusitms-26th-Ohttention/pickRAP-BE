package pickRAP.server.repository.hashtag;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import pickRAP.server.controller.dto.analysis.HashTagResponse;
import pickRAP.server.controller.dto.analysis.HashtagFilterCondition;
import pickRAP.server.controller.dto.analysis.QHashTagResponse;

import java.time.LocalDateTime;
import java.util.List;

import static pickRAP.server.domain.hashtag.QHashtag.*;
import static pickRAP.server.domain.member.QMember.*;

@RequiredArgsConstructor
public class HashtagRepositoryImpl implements HashtagRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<HashTagResponse> getHashtagAnalysisResults(HashtagFilterCondition hashtagFilterCond, String email) {
        List<HashTagResponse> hashTagResponses = jpaQueryFactory
                .select(new QHashTagResponse(hashtag.tag, hashtag.id.count().longValue()))
                .from(hashtag)
                .join(hashtag.member, member)
                .where(member.email.eq(email), timeFilter(hashtagFilterCond))
                .groupBy(hashtag.tag)
                .orderBy(hashtag.id.count().desc())
                .limit(3)
                .offset(0)
                .fetch();

        long total = jpaQueryFactory
                .selectFrom(hashtag)
                .join(hashtag.member, member)
                .where(member.email.eq(email), timeFilter(hashtagFilterCond))
                .fetchCount();

        return getHashTagResponses(hashTagResponses, total);
    }


    // 전체:all, 3개월:recent, 년:year, 월:month
    private BooleanExpression timeFilter(HashtagFilterCondition hashtagFilterCond) {
        String filter = hashtagFilterCond.getFilter();
        Integer year = hashtagFilterCond.getYear();
        Integer month = hashtagFilterCond.getMonth();


        if (filter.equals("all")) {
            return null;
        } else if (filter.equals("recent")){
            return hashtag.createTime.after(LocalDateTime.now().minusMonths(3));
        } else if (filter.equals("year")){
            LocalDateTime startYearDate = LocalDateTime.of(year, 1, 1, 0, 0);
            return hashtag.createTime.goe(startYearDate).and(hashtag.createTime.lt(startYearDate.plusYears(1)));
        } else if (filter.equals("month")) {
            LocalDateTime startMonthDate = LocalDateTime.of(year, month, 1, 0, 0);
            return hashtag.createTime.goe(startMonthDate)
                    .and(hashtag.createTime.lt(startMonthDate.plusMonths(1)));
        }
        return null;
    }

    private List<HashTagResponse> getHashTagResponses(List<HashTagResponse> hashTagResponses, long total) {
        if (hashTagResponses.isEmpty()) {
            return hashTagResponses;
        }

        int sum = 0;
        for (HashTagResponse hashTagResponse : hashTagResponses) {
            hashTagResponse.setRate(getRate(hashTagResponse.getCount(), total));
            sum += hashTagResponse.getCount();
        }

        // 기타
        hashTagResponses.add(new HashTagResponse("기타", total-sum));
        hashTagResponses.get(hashTagResponses.size() - 1).setRate(getRate(total-sum, total));

        return hashTagResponses;
    }

    private long getRate(long count, long total) {
        return (long) ((count/(double)total)*100);
    }


}
