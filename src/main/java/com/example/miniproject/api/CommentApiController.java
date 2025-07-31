package com.example.miniproject.api;

import com.example.miniproject.dto.CommentDto;
import com.example.miniproject.entity.Comment;
import com.example.miniproject.entity.Member;
import com.example.miniproject.repository.MemberRepository;
import com.example.miniproject.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
public class CommentApiController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private MemberRepository memberRepository;

    // 1. 댓글 조회
    @GetMapping("/api/articles/{articleId}/comments")
    public ResponseEntity<List<CommentDto>> comments(@PathVariable Long articleId) {
        // 서비스에 위임
        List<CommentDto> dtos = commentService.comments(articleId);
        // 결과 응답
        return ResponseEntity.status(HttpStatus.OK).body(dtos);
    }

    // 2. 댓글 생성
    @PostMapping("/api/articles/{articleId}/comments")
    public ResponseEntity<CommentDto> create(@PathVariable Long articleId, @RequestBody CommentDto dto,
                                             Principal principal) {
        // 로그인 유저 이메일로 Member 조회
        String email = principal.getName(); // SecurityConfig에서 usernameParameter로 email 사용 중
        Member member = memberRepository.findByEmail(email);
        // 2. nickname 설정
        dto.setNickname(member.getNickname());

        // 서비스에 위임
        CommentDto createdDto = commentService.create(articleId, dto);
        // 결과 응답
        return ResponseEntity.status(HttpStatus.OK).body(createdDto);
    }

    // 3. 댓글 수정
    @PatchMapping("/api/comments/{id}")
    public ResponseEntity<CommentDto> update(@PathVariable Long id,
                                             @RequestBody CommentDto dto) {
        // 서비스에 위임
        CommentDto updateDto = commentService.update(id, dto);
        // 결과 응답
        return ResponseEntity.status(HttpStatus.OK).body(updateDto);
    }

    // 4. 댓글 삭제
    @DeleteMapping("/api/comments/{id}")
    public ResponseEntity<CommentDto> delete(@PathVariable Long id) {

        // 서비스에 위임
        CommentDto deletedDto = commentService.delete(id);
        // 결과 응답
        return ResponseEntity.status(HttpStatus.OK).body(deletedDto);
    }
}
