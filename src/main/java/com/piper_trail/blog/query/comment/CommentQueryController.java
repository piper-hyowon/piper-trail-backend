package com.piper_trail.blog.query.comment;

import com.piper_trail.blog.command.comment.CommentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping()
@RequiredArgsConstructor
public class CommentQueryController {
  private final CommentQueryService commentQueryService;

  @GetMapping("posts/{postId}/comments")
  public ResponseEntity<List<CommentResponse>> getComments(@PathVariable String postId) {
    return ResponseEntity.ok(commentQueryService.getCommentsByPostId(postId));
  }

  @GetMapping("/comments")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<List<CommentAdminResponse>> getAllComments() {
    return ResponseEntity.ok(commentQueryService.getAllComments());
  }
}
