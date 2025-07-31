package com.example.miniproject.controller;

import com.example.miniproject.config.CustomOAuth2User;
import com.example.miniproject.config.CustomUserDetails;
import com.example.miniproject.dto.ArticleForm;
import com.example.miniproject.dto.CommentDto;
import com.example.miniproject.entity.Article;
import com.example.miniproject.entity.Member;
import com.example.miniproject.repository.ArticleRepository;
import com.example.miniproject.repository.CommentRepository;
import com.example.miniproject.repository.MemberRepository;
import com.example.miniproject.service.CommentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Controller
@Slf4j
public class ArticleController {

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private CommentService commentService;

    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private CommentRepository commentRepository;

    @GetMapping("/articles/new")
    public String newArticleForm() {
        return "articles/new";
    }

    @PostMapping("/articles/create")
    public String createArticle(ArticleForm form) {
        log.info(form.toString());

        // 1. DTO를 엔티티로 변환
        Article article = form.toEntity();
        log.info(article.toString());

        // 2. 리파지터리로 엔티티를 DB에 저장
        Article saved = articleRepository.save(article);
        log.info(saved.toString());
        return "redirect:/articles/" + saved.getId();
    }

    @GetMapping("/articles/{id}")
    public String show(@PathVariable Long id, Model model, Principal principal) {

        String nickname = "익명";
        if (principal != null) {
            String email = principal.getName();
            log.info(email);
            Member member = memberRepository.findByEmail(email);
            if (member != null && member.getNickname() != null) {
                nickname = member.getNickname();
            }
        }

        log.info("id = " + id);
        // id를 조회해 데이터 가져오기
        Article articleEntity = articleRepository.findById(id).orElse(null);

        // 작성자 닉네임
        String authorNickname = "알 수 없음";
        if (articleEntity != null) {
            Member author = memberRepository.findByEmail(articleEntity.getCreatedBy());
            authorNickname = author.getNickname();
        }

        // 조회수 증가
        articleEntity.setViewCount(articleEntity.getViewCount() + 1);
        articleRepository.save(articleEntity);

        List<CommentDto> commentsDtos = commentService.comments(id);

        model.addAttribute("article", articleEntity);
        model.addAttribute("commentDtos", commentsDtos);
        model.addAttribute("commentCount", commentsDtos.size());

        model.addAttribute("nickname", nickname);

        model.addAttribute("authorNickname", authorNickname);

        return "articles/show";
    }

    @GetMapping("/articles")
    public String index(Model model, Principal principal) {

        // 1. 모든 데이터 가져오기
        List<Article> articleEntityList = articleRepository.findAllByOrderByIdDesc();

        // 2. 작성자 닉네임 매핑
        Map<Long, String> nicknameMap = new HashMap<>();
        Map<Long, Integer> commentCountMap = new HashMap<>();

        for (Article article : articleEntityList) {
            String email = article.getCreatedBy();
            Member member = memberRepository.findByEmail(email);
            String nickname = (member != null && member.getNickname() != null) ? member.getNickname() : "익명";
            nicknameMap.put(article.getId(), nickname);

            int count = commentRepository.countByArticleId(article.getId());
            commentCountMap.put(article.getId(), count);
        }

        // 2. 모델에 데이터 등록하기
        model.addAttribute("articleList", articleEntityList);
        model.addAttribute("nicknameMap", nicknameMap);
        model.addAttribute("commentCountMap", commentCountMap);

        return "articles/index";
    }

    @GetMapping("/articles/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        // 수정할 데이터 가져오기
        Article articleEntity = articleRepository.findById(id).orElse(null);
        // 모델에 데이터 등록하기
        model.addAttribute("article", articleEntity);
        // 뷰 페이지 설정하기
        return "articles/edit";
    }

    @PostMapping("/articles/update")
    public String update(ArticleForm form,
                         Principal principal,
                         RedirectAttributes rttr) {

        Article articleEntity = form.toEntity();
        Article target = articleRepository.findById(articleEntity.getId()).orElse(null);

        if (principal == null || target == null) {
            rttr.addFlashAttribute("msg", "오류가 발생했습니다.");
            return "redirect:/articles";
        }

        String currentEmail = principal.getName();
        if (!target.getCreatedBy().equals(currentEmail)) {
            rttr.addFlashAttribute("msg", "작성자만 수정할 수 있습니다.");
            return "redirect:/articles/" + articleEntity.getId();
        }

        target.patch(articleEntity);
        articleRepository.save(target);

        return "redirect:/articles/" + articleEntity.getId();
    }

    @GetMapping("/articles/{id}/delete")
    public String delete(@PathVariable Long id,
                         RedirectAttributes rttr,
                         @AuthenticationPrincipal Object principal) {

        log.info("삭제 요청이 들어왔습니다!");
        Article target = articleRepository.findById(id).orElse(null);

        if (target == null) {
            return "redirect:/articles";
        }

        String currentEmail = null;

        if (principal instanceof CustomUserDetails) {
            currentEmail = ((CustomUserDetails) principal).getUsername();
        } else if (principal instanceof CustomOAuth2User) {
            currentEmail = ((CustomOAuth2User) principal).getMember().getEmail();
        }

        if (currentEmail == null || !target.getCreatedBy().equals(currentEmail)) {
            rttr.addFlashAttribute("msg", "작성자만 삭제할 수 있습니다.");
            return "redirect:/articles/" + id;
        }

        articleRepository.delete(target);
        rttr.addFlashAttribute("msg", "삭제됐습니다.");
        return "redirect:/articles";
    }
}
