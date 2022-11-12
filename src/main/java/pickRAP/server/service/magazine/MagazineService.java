package pickRAP.server.service.magazine;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pickRAP.server.common.BaseException;
import pickRAP.server.common.BaseExceptionStatus;
import pickRAP.server.controller.dto.magazine.MagazineRequest;
import pickRAP.server.domain.magazine.Magazine;
import pickRAP.server.domain.magazine.MagazinePage;
import pickRAP.server.domain.magazine.MagazineTemplate;
import pickRAP.server.domain.member.Member;
import pickRAP.server.repository.magazine.MagazinePageRepository;
import pickRAP.server.repository.magazine.MagazineRepository;
import pickRAP.server.repository.member.MemberRepository;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class MagazineService {

    private final MemberRepository memberRepository;
    private final MagazineRepository magazineRepository;
    private final MagazinePageRepository magazinePageRepository;

    @Transactional
    public void save(MagazineRequest request, String email, String template) {
        Member writer = memberRepository.findByEmail(email).orElseThrow();

        Magazine magazine = Magazine.builder()
                .title(request.getTitle())
                .openStatus(request.isOpenStatus())
                .template(MagazineTemplate.valueOf(template.toUpperCase(Locale.ROOT)))
                .build();

        magazine.setMember(writer);

        magazineRepository.save(magazine);

        request.getPageList().forEach(p -> {
            if(p.getText().length() > 1000) {
                throw new BaseException(BaseExceptionStatus.EXCEED_TEXT_LENGTH);
            }
            MagazinePage page = MagazinePage.builder()
                    .text(p.getText())
                    .build();
            // 스크랩 콘텐츠 가져와서 MagazinePage와 세팅
            // Scrap scrap = scrapRepository.findById(page.getScrapId()).orElseThrow();
            // page.setScrap(scrap);

            page.setMagazine(magazine);
            magazinePageRepository.save(page);
        });
        return;
    }
}
