package pickRAP.server.service.magazine;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pickRAP.server.common.BaseException;
import pickRAP.server.common.BaseExceptionStatus;
import pickRAP.server.controller.dto.magazine.*;
import pickRAP.server.domain.magazine.Color;
import pickRAP.server.domain.magazine.ColorType;
import pickRAP.server.domain.magazine.Magazine;
import pickRAP.server.domain.magazine.MagazinePage;
import pickRAP.server.domain.member.Member;
import pickRAP.server.domain.scrap.Scrap;
import pickRAP.server.domain.scrap.ScrapType;
import pickRAP.server.repository.color.ColorRepository;
import pickRAP.server.repository.magazine.MagazinePageRepository;
import pickRAP.server.repository.magazine.MagazineRepository;
import pickRAP.server.repository.magazine.MagazineRepositoryCustom;
import pickRAP.server.repository.member.MemberRepository;
import pickRAP.server.repository.scrap.ScrapRepository;
import pickRAP.server.service.text.TextService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static pickRAP.server.common.BaseExceptionStatus.*;

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
    private final TextService textService;
    private final ColorRepository colorRepository;

    @Transactional
    public void save(MagazineRequest request, String email) {
        if(request.getTitle().length() > MAX_TITLE_LENGTH) {
            throw new BaseException(EXCEED_TITLE_LENGTH);
        }

        Member member = memberRepository.findByEmail(email).orElseThrow();

        Optional<Scrap> cover = scrapRepository.findById(request.getCoverScrapId());
        if(!cover.isPresent()) {
            throw new BaseException(DONT_EXIST_SCRAP);
        }
        if(cover.get().getScrapType() != ScrapType.IMAGE) {
            throw new BaseException(DONT_MATCH_TYPE);
        }

        Magazine magazine = Magazine.builder()
                .title(request.getTitle())
                .openStatus(request.isOpenStatus())
                .member(member)
                .cover(cover.get().getFileUrl())
                .build();

        saveMagazinePages(request.getPageList(), magazine, member);

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
                        .previewUrl(p.getScrap().getPreviewUrl())
                        .text(p.getText()).build());
            } else if (p.getScrap().getScrapType() == ScrapType.LINK) {
                magazinePages.add(MagazinePageResponse.builder()
                        .pageId(p.getId())
                        .previewUrl(p.getScrap().getPreviewUrl())
                        .contents(p.getScrap().getContent())
                        .text(p.getText()).build());
            } else {
                magazinePages.add(MagazinePageResponse.builder()
                        .pageId(p.getId())
                        .contents(p.getScrap().getContent())
                        .text(p.getText()).build());
            }
        }

        List<ColorType> magazineColors = colorRepository.getMagazineColors(findMagazine);

        MagazineResponse magazine = MagazineResponse.builder()
                .magazineId(findMagazine.getId())
                .title(findMagazine.getTitle())
                .openStatus(findMagazine.isOpenStatus())
                .createdDate(findMagazine.getCreateTime())
                .pageList(magazinePages)
                .colors(magazineColors.stream().map(c -> c.getValue()).collect(Collectors.toList()))
                .build();

        return magazine;
    }

    @Transactional
    public void updateMagazine(MagazineRequest request, Long magazineId, String email) {
        if(request.getTitle().length() > MAX_TITLE_LENGTH) {
            throw new BaseException(EXCEED_TITLE_LENGTH);
        }
        Magazine findMagazine = magazineRepository.findById(magazineId).orElseThrow();

        Member member = memberRepository.findByEmail(email).orElseThrow();

        checkMatchWriter(findMagazine, email);

        Optional<Scrap> cover = scrapRepository.findById(request.getCoverScrapId());
        if(!cover.isPresent()) {
            throw new BaseException(DONT_EXIST_SCRAP);
        }
        if(cover.get().getScrapType() != ScrapType.IMAGE) {
            throw new BaseException(DONT_MATCH_TYPE);
        }

        findMagazine.updateMagazine(request.getTitle(), request.isOpenStatus(), cover.get().getFileUrl());

        deleteTextByMagazine(findMagazine, email);

        magazinePageRepository.deleteByMagazineId(findMagazine.getId());

        saveMagazinePages(request.getPageList(), findMagazine, member);

        return;
    }

    @Transactional
    public void saveMagazinePages(List<MagazinePageRequest> requestList, Magazine magazine, Member member) {
        requestList.forEach(p -> {
            if (p.getText().length() > MAX_TEXT_LENGTH) {
                throw new BaseException(EXCEED_TEXT_LENGTH);
            }

            Optional<Scrap> scrap = scrapRepository.findById(p.getScrapId());
            if(!scrap.isPresent()) {
                throw new BaseException(DONT_EXIST_SCRAP);
            }

            MagazinePage page = MagazinePage.builder()
                    .scrap(scrap.get())
                    .text(p.getText())
                    .magazine(magazine)
                    .build();

            textService.save(member, page.getText());

            magazinePageRepository.save(page);
        });

    }

    public void checkMatchWriter(Magazine magazine, String email) {
        if(!magazine.checkWriter(email)) {
            new BaseException(NOT_MATCH_WRITER);
        }
    }

    @Transactional
    public void deleteMagazine(Long magazineId, String email) {
        Magazine findMagazine = magazineRepository.findById(magazineId).orElseThrow();

        checkMatchWriter(findMagazine, email);

        deleteTextByMagazine(findMagazine, email);

        magazineRepository.delete(findMagazine);
    }

    public void deleteTextByMagazine(Magazine magazine, String email) {
        Member member = memberRepository.findByEmail(email).orElseThrow();
        List<String> texts = magazinePageRepository.findTextByMagazine(magazine);

        texts.forEach(t -> textService.delete(member, t));
    }

    @Transactional
    public void deletePage(Long pageId, String email) {
        deleteTextByPage(pageId, email);

        magazinePageRepository.deleteById(pageId);
    }

    public void deleteTextByPage(Long pageId, String email) {
        Member member = memberRepository.findByEmail(email).orElseThrow();
        String text = magazinePageRepository.findTextById(pageId);

        textService.delete(member, text);
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

    public MagazineColorResponse getMagazineColor(String email, Long magazineId) {
        Member member = memberRepository.findByEmail(email).orElseThrow();
        Magazine magazine = magazineRepository.findById(magazineId).orElseThrow();

        if (magazine.getMember() == member) {
            throw new BaseException(CANT_COLOR_REACTION);
        }

        Optional<Color> findColor = colorRepository.findByMemberAndMagazine(member, magazine);

        if (findColor.isEmpty()) {
            return MagazineColorResponse.builder().colorType(null).build();
        }

        Color color = findColor.get();
        return MagazineColorResponse.builder().colorType(color.getColorType().getValue()).build();
    }

    @Transactional
    public void addMagazineColor(String email, Long magazineId, MagazineColorRequest magazineColorRequest) {
        Member member = memberRepository.findByEmail(email).orElseThrow();
        Magazine magazine = magazineRepository.findById(magazineId).orElseThrow();

        ColorType colorType = ColorType.from(magazineColorRequest.getColorType());

        Optional<Color> findColor = colorRepository.findByMemberAndMagazine(member, magazine);

        if (findColor.isEmpty()) {
            colorRepository.save(Color.builder()
                    .colorType(colorType)
                    .member(member)
                    .magazine(magazine)
                    .build());
        } else if (findColor.get().getColorType() == colorType) {
            colorRepository.delete(findColor.get());
        } else {
            findColor.get().updateColor(colorType);
        }
    }
}
