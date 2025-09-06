package com.piper_trail.blog.query.comment;

import com.piper_trail.blog.command.comment.CommentResponse;
import com.piper_trail.blog.shared.dto.PagedResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping()
@RequiredArgsConstructor
public class CommentQueryController {
  private final CommentQueryService commentQueryService;

  @GetMapping("posts/{postId}/comments")
  public ResponseEntity<PagedResponse<CommentResponse>> getComments(
      @PathVariable String postId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    return ResponseEntity.ok(
        commentQueryService.getCommentsByPostId(postId, PageRequest.of(page, size)));
  }

  // TODO: 관리자 대시보드에서 사용
  @GetMapping("/comments")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<List<CommentAdminResponse>> getAllComments() {
    return ResponseEntity.ok(commentQueryService.getAllComments());
  }
}
