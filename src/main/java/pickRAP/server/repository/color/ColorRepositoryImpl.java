package pickRAP.server.repository.color;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import pickRAP.server.controller.dto.analysis.PersonalMoodResponse;
import pickRAP.server.domain.magazine.ColorType;
import pickRAP.server.domain.magazine.Magazine;
import pickRAP.server.domain.member.Member;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static pickRAP.server.domain.magazine.QColor.*;
import static pickRAP.server.domain.magazine.QMagazine.*;

@RequiredArgsConstructor
public class ColorRepositoryImpl implements ColorRepositoryCustom{

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<ColorType> getMagazineColors(Magazine magazine) {
        return jpaQueryFactory
                .select(color.colorType)
                .from(color)
                .where(color.magazine.eq(magazine))
                .groupBy(color.colorType)
                .orderBy(color.colorType.count().desc())
                .limit(3)
                .offset(0)
                .fetch();
    }


    @Override
    public List<PersonalMoodResponse> getPersonalMoodAnalysisResults(Member member) {
        List<PersonalMoodResult> personalMoodResponses = jpaQueryFactory
                .select(new QPersonalMoodResult(color.colorType, color.colorType.count().longValue()))
                .from(color)
                .join(color.magazine, magazine)
                .where(magazine.member.eq(member))
                .groupBy(color.colorType)
                .orderBy(color.colorType.count().desc())
                .limit(4)
                .offset(0)
                .fetch();


        long total = jpaQueryFactory
                .selectFrom(color)
                .join(color.magazine, magazine)
                .where(magazine.member.eq(member))
                .fetchCount();


        return getPersonalMoodResponses(personalMoodResponses, total);
    }

    private List<PersonalMoodResponse> getPersonalMoodResponses(List<PersonalMoodResult> personalMoodResults, long total) {
        if (personalMoodResults.isEmpty()) {
            return new ArrayList<>();
        }

        return personalMoodResults
                .stream()
                .map(c -> new PersonalMoodResponse(c.getColorStyle().getValue(), c.getCount(), total))
                .collect(Collectors.toList());
    }



}
