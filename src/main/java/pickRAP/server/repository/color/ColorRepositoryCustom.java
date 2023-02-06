package pickRAP.server.repository.color;

import pickRAP.server.controller.dto.analysis.PersonalMoodResponse;
import pickRAP.server.domain.magazine.ColorType;
import pickRAP.server.domain.magazine.Magazine;
import pickRAP.server.domain.member.Member;

import java.util.List;

public interface ColorRepositoryCustom {

    List<ColorType> getMagazineColors(Magazine magazine);

    List<PersonalMoodResponse> getPersonalMoodAnalysisResults(Member member);

}
