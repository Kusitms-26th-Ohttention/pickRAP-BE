package pickRAP.server.repository.hashtag;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import pickRAP.server.controller.dto.Hashtag.HashtagResponse;
import pickRAP.server.controller.dto.Hashtag.QHashtagResponse;
import pickRAP.server.controller.dto.analysis.HashTagResponse;
import pickRAP.server.controller.dto.analysis.HashtagFilterCondition;
import pickRAP.server.controller.dto.analysis.QHashTagResponse;
import pickRAP.server.domain.member.Member;

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

        long sum = 0;
        long rate = 100;

        for (HashTagResponse hashTagResponse : hashTagResponses) {
            long eachCount = hashTagResponse.getCount();
            long eachRate = getRate(eachCount, total);

            sum += eachCount;
            hashTagResponse.setRate(eachRate);
            rate -= eachRate;
        }

        // 기타
        long remainderRate = getRate(total-sum, total);

        hashTagResponses.add(new HashTagResponse("기타", total-sum));
        hashTagResponses.get(hashTagResponses.size() - 1).setRate(remainderRate);
        rate -= remainderRate;

        // 비율 맞추기
        HashTagResponse firstHashtag = hashTagResponses.get(0);
        firstHashtag.setRate(firstHashtag.getRate() + rate);

        return hashTagResponses;
    }

    private long getRate(long count, long total) {
        return (long) ((count/(double)total)*100);
    }

    @Override
    public Slice<HashtagResponse> findSliceHashtagResponse(Member member, Pageable pageable) {
        List<HashtagResponse> hashtagResponses = jpaQueryFactory
                .select(new QHashtagResponse(hashtag.tag, hashtag.usedInProfile))
                .from(hashtag)
                .where(hashtag.member.eq(member))
                .groupBy(hashtag.tag, hashtag.usedInProfile)
                .orderBy(hashtag.usedInProfile.desc(), hashtag.tag.asc())
                .offset(pageable.getPageNumber())
                .limit(pageable.getPageSize() + 1)
                .fetch();

        return createSliceHashtagResponse(hashtagResponses, pageable);
    }

    private Slice<HashtagResponse> createSliceHashtagResponse(List<HashtagResponse> hashtagResponses, Pageable pageable) {
        boolean hasNext = false;

        if (hashtagResponses.size() > pageable.getPageSize()) {
            hasNext = true;
            hashtagResponses.remove(pageable.getPageSize());
        }

        return new SliceImpl<>(hashtagResponses, pageable, hasNext);
    }
}
