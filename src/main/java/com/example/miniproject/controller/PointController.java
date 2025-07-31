package com.example.miniproject.controller;

import com.example.miniproject.config.CustomOAuth2User;
import com.example.miniproject.config.CustomUserDetails;
import com.example.miniproject.dto.ArticleDto;
import com.example.miniproject.dto.CommentDto;
import com.example.miniproject.dto.PointFormDto;
import com.example.miniproject.entity.Article;
import com.example.miniproject.entity.Comment;
import com.example.miniproject.entity.Member;
import com.example.miniproject.entity.PointHistory;
import com.example.miniproject.repository.ArticleRepository;
import com.example.miniproject.repository.CommentRepository;
import com.example.miniproject.repository.MemberRepository;
import com.example.miniproject.repository.PointHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@Slf4j
public class PointController {

    private final MemberRepository memberRepository;
    private final PointHistoryRepository pointHistoryRepository;
    private final ArticleRepository articleRepository;
    private final CommentRepository commentRepository;

    @GetMapping("/point/charge")
    public String showChargePage(Model model) {
        model.addAttribute("pointForm", new PointFormDto());
        return "point/charge";
    }

    @PostMapping("/point/charge")
    public String charge(@ModelAttribute PointFormDto pointForm,
                         @AuthenticationPrincipal(expression = "member")Member member,
                         Model model) {

        int amount = pointForm.getAmount();
        if (amount <= 0) {
            model.addAttribute("error", "0보다 큰 숫자를 입력하세요.");
            return "point/charge";
        }

        member.setPoint(member.getPoint() + amount);
        memberRepository.save(member);


        PointHistory history = PointHistory.builder()
                .member(member)
                .amount(amount)
                .description("포인트 충전")
                .build();

        pointHistoryRepository.save(history);

        model.addAttribute("msg", amount + "P가 충전되었습니다.");
        return "redirect:/myPage";
    }

    @GetMapping("/point/history")
    public String viewHistory(@AuthenticationPrincipal Object principal,
                              Model model) {
        Member member = null;

        if (principal instanceof CustomUserDetails userDetails) {
            member = userDetails.getMember();
        } else if (principal instanceof CustomOAuth2User oAuth2User) {
            member = oAuth2User.getMember();
        }

        if (member == null) {
            return "redirect:/members/login";
        }

        List<PointHistory> historyList = pointHistoryRepository
                .findByMemberIdOrderByRegTimeDesc(member.getId());
        model.addAttribute("historyList", historyList);
        model.addAttribute("currentPoint", member.getPoint());

        return "point/history";
    }

    @GetMapping("/myPage")
    public String myPage(@AuthenticationPrincipal(expression = "member") Member member,
                         @RequestParam(name = "commentPage", defaultValue = "0") int commentPage,
                         @RequestParam(name = "articlePage", defaultValue = "0") int articlePage,
                         Model model) {

        String email = member.getEmail();
        String nickname = member.getNickname();

        Pageable articlePageable = PageRequest.of(articlePage, 3, Sort.by(Sort.Direction.DESC, "regTime"));
        Page<Article> articlePageResult = articleRepository.findByCreatedBy(email, articlePageable);
        Page<ArticleDto> articleDtoPage = articlePageResult.map(
                article -> ArticleDto.fromEntity(article, nickname)
        );

        Pageable commentPageable = PageRequest.of(commentPage, 3, Sort.by(Sort.Direction.DESC, "regTime"));
        Page<Comment> commentPageResult = commentRepository.findByCreatedBy(email, commentPageable);
        List<CommentDto> myCommentDtos = commentPageResult.getContent().stream()
                .map(CommentDto::createCommentDto)
                .toList();

        log.info("댓글 페이지 사이즈 = {}", commentPageResult.getSize());
        log.info("댓글 개수 = {}", myCommentDtos.size());


        model.addAttribute("member", member);
        model.addAttribute("myComments", myCommentDtos);
        model.addAttribute("commentPage", commentPageResult);
        model.addAttribute("articlePage", articleDtoPage);

        return "point/myPage";
    }

}
