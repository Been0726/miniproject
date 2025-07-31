package com.example.miniproject.service;

import com.example.miniproject.dto.ArticleForm;
import com.example.miniproject.entity.Article;
import com.example.miniproject.repository.ArticleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.access.AccessDeniedException;



@Service
@Slf4j
public class ArticleService {

    @Autowired
    ArticleRepository articleRepository;

    public List<Article> index() {
        return articleRepository.findAll();
    }

    public Article show(Long id) {
        return articleRepository.findById(id).orElse(null);
    }

    public Article create(ArticleForm dto) {
        Article article = dto.toEntity();
        log.info(article.toString());
        if (article.getId() != null) {
            return null;
        }
        return articleRepository.save(article);
    }

    public Article update(Long id, ArticleForm dto, String currentUserEmail) {
        // 1. DTO -> 엔티티 조회하기
        Article article = dto.toEntity();
        log.info("id : {}, article : {}", id, article.toString());
        // 2. 타깃 조회하기
        Article target = articleRepository.findById(id).orElse(null);
        // 3. 잘못된 요청 처리하기
        if (target == null || id != article.getId()) {
            log.info("잘못된 요청! id : {}, article : {}", id, article.toString());
            return null;
        }

        // 작성자 확인
        if (!target.getCreatedBy().equals(currentUserEmail)) {
            throw new AccessDeniedException("작성자만 수정할 수 있습니다.");
        }

        // 4. 업데이트 및 정상 응답(200)하기
        target.patch(article);
        Article updated = articleRepository.save(target);
        return updated;
    }

    public Article delete(Long id, String currentUserEmail) {
        // 1. 대상 찾기
        Article target = articleRepository.findById(id).orElse(null);
        // 2. 잘못된 요청 처리하기
        if (target == null) {
            return null;
        }

        if (!target.getCreatedBy().equals(currentUserEmail)) {
            throw new AccessDeniedException("작성자만 삭제할 수 있습니다.");
        }

        // 3. 대상 삭제하기
        articleRepository.delete(target);
        return target;
    }

    @Transactional
    public List<Article> createArticles(List<ArticleForm> dtos) {
        // 1. dto 묶음을 엔티티 묶음으로 변환하기
        List<Article> articleList = dtos.stream()
                .map(dto -> dto.toEntity())
                .collect(Collectors.toList());
        // 2. 엔티티 묶음을 DB에 저장하기
        articleList.stream()
                .forEach(article -> articleRepository.save(article));
        // 3. 강제 예외 발생시키기
        articleRepository.findById(-1L)
                .orElseThrow(() -> new IllegalStateException("결제 실패"));
        // 4. 결과 값 반환하기
        return articleList;
    }
}
