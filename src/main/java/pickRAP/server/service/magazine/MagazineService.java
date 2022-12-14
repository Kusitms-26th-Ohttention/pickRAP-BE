package pickRAP.server.service.magazine;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pickRAP.server.common.BaseException;
import pickRAP.server.common.BaseExceptionStatus;
import pickRAP.server.common.URLPreview;
import pickRAP.server.controller.dto.magazine.*;
import pickRAP.server.domain.magazine.Magazine;
import pickRAP.server.domain.magazine.MagazinePage;
import pickRAP.server.domain.member.Member;
import pickRAP.server.domain.scrap.Scrap;
import pickRAP.server.domain.scrap.ScrapType;
import pickRAP.server.repository.magazine.MagazinePageRepository;
import pickRAP.server.repository.magazine.MagazineRepository;
import pickRAP.server.repository.magazine.MagazineRepositoryCustom;
import pickRAP.server.repository.member.MemberRepository;
import pickRAP.server.repository.scrap.ScrapRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MagazineService {

    final static int MAX_TEXT_LENGTH = 200;
    final static int MAX_TITLE_LENGTH = 15;

    private final MemberRepository memberRepository;
    private final MagazineRepository magazineRepository;
    private final MagazineRepositoryCustom magazineRepositoryCustom;
    private final MagazinePageRepository magazinePageRepository;
    private final ScrapRepository scrapRepository;

    @Transactional
    public void save(MagazineRequest request, String email) {
        if(request.getTitle().length() > MAX_TITLE_LENGTH) {
            throw new BaseException(BaseExceptionStatus.EXCEED_TITLE_LENGTH);
        }

        Member member = memberRepository.findByEmail(email).orElseThrow();

        Optional<Scrap> cover = scrapRepository.findById(request.getCoverScrapId());
        if(!cover.isPresent()) {
            throw new BaseException(BaseExceptionStatus.DONT_EXIST_SCRAP);
        }
        if(cover.get().getScrapType() != ScrapType.IMAGE) {
            throw new BaseException(BaseExceptionStatus.DONT_MATCH_TYPE);
        }

        Magazine magazine = Magazine.builder()
                .title(request.getTitle())
                .openStatus(request.isOpenStatus())
                .member(member)
                .cover(cover.get().getFileUrl())
                .build();

        saveMagazinePages(request.getPageList(), magazine);

        return;
    }

    @Transactional(readOnly = true)
    public List<MagazineListResponse> findMagazines(String email) {
        List<Magazine> findMagazines = magazineRepositoryCustom.findMemberMagazines(email);

        List<MagazineListResponse> collect = findMagazines.stream()
                .map(m -> MagazineListResponse.builder()
                        .magazineId(m.getId())
                        .coverUrl(m.getCover())
                        .title(m.getTitle())
                        .build())
                .collect(Collectors.toList());

        return collect;
    }

    @Transactional(readOnly = true)
    public MagazineResponse findMagazine(Long magazineId) {
        Magazine findMagazine = magazineRepository.findById(magazineId).orElseThrow();

        List<MagazinePage> findMagazinePages = findMagazine.getPages();
        List<MagazinePageResponse> magazinePages = new ArrayList<>();

        for(MagazinePage p : findMagazinePages) {
            if(p.getScrap().getScrapType() == ScrapType.IMAGE
                    || p.getScrap().getScrapType() == ScrapType.VIDEO
                    || p.getScrap().getScrapType() == ScrapType.PDF) {
                magazinePages.add(MagazinePageResponse.builder()
                        .pageId(p.getId())
                        .fileUrl(p.getScrap().getFileUrl())
                        .text(p.getText()).build());
            } else if (p.getScrap().getScrapType() == ScrapType.LINK) {
                magazinePages.add(MagazinePageResponse.builder()
                        .pageId(p.getId())
                        .contents(URLPreview.getLinkPreviewInfo(p.getScrap().getContent()))
                        .text(p.getText()).build());
            } else {
                magazinePages.add(MagazinePageResponse.builder()
                        .pageId(p.getId())
                        .contents(p.getScrap().getContent())
                        .text(p.getText()).build());
            }
        }

        MagazineResponse magazine = MagazineResponse.builder()
                .magazineId(findMagazine.getId())
                .title(findMagazine.getTitle())
                .openStatus(findMagazine.isOpenStatus())
                .createdDate(findMagazine.getCreateTime())
                .pageList(magazinePages)
                .build();

        return magazine;
    }

    @Transactional
    public void updateMagazine(MagazineRequest request, Long magazineId, String email) {
        if(request.getTitle().length() > MAX_TITLE_LENGTH) {
            throw new BaseException(BaseExceptionStatus.EXCEED_TITLE_LENGTH);
        }
        Magazine findMagazine = magazineRepository.findById(magazineId).orElseThrow();

        Member member = memberRepository.findByEmail(email).orElseThrow();

        checkMatchWriter(findMagazine, email);

        Optional<Scrap> cover = scrapRepository.findById(request.getCoverScrapId());
        if(!cover.isPresent()) {
            throw new BaseException(BaseExceptionStatus.DONT_EXIST_SCRAP);
        }
        if(cover.get().getScrapType() != ScrapType.IMAGE) {
            throw new BaseException(BaseExceptionStatus.DONT_MATCH_TYPE);
        }

        findMagazine.updateMagazine(request.getTitle(), request.isOpenStatus(), cover.get().getFileUrl());

        magazinePageRepository.deleteByMagazineId(findMagazine.getId());

        saveMagazinePages(request.getPageList(), findMagazine);

        return;
    }

    @Transactional
    public void saveMagazinePages(List<MagazinePageRequest> requestList, Magazine magazine) {
        requestList.forEach(p -> {
            if (p.getText().length() > MAX_TEXT_LENGTH) {
                throw new BaseException(BaseExceptionStatus.EXCEED_TEXT_LENGTH);
            }

            Optional<Scrap> scrap = scrapRepository.findById(p.getScrapId());
            if(!scrap.isPresent()) {
                throw new BaseException(BaseExceptionStatus.DONT_EXIST_SCRAP);
            }

            MagazinePage page = MagazinePage.builder()
                    .scrap(scrap.get())
                    .text(p.getText())
                    .magazine(magazine)
                    .build();

            magazinePageRepository.save(page);
        });

    }

    public void checkMatchWriter(Magazine magazine, String email) {
        if(!magazine.checkWriter(email)) {
            new BaseException(BaseExceptionStatus.NOT_MATCH_WRITER);
        }
    }

    @Transactional
    public void deleteMagazine(Long magazineId, String email) {
        Magazine findMagazine = magazineRepository.findById(magazineId).orElseThrow();

        checkMatchWriter(findMagazine, email);

        magazineRepository.delete(findMagazine);
    }

    @Transactional
    public void deletePage(Long pageId) {
        magazinePageRepository.deleteById(pageId);
    }

    @Transactional(readOnly = true)
    public boolean isExistMagazineTitle(String title, String email) {
        Member member = memberRepository.findByEmail(email).orElseThrow();

        Optional<Magazine> findMagazine = magazineRepository.findByTitleAndMember(title, member);
        if(findMagazine.isPresent()) {
            return true;
        }
        return false;
    }
}
