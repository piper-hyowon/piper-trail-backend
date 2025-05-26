package com.piper_trail.blog.command.comment;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/posts/{postId}/comments")
@RequiredArgsConstructor
@Slf4j
public class CommentCommandController {

  private final CommentCommandService commentCommandService;

  @PostMapping
  public ResponseEntity<CommentResponse> createComment(
      @PathVariable String postId,
      @RequestBody @Valid CreateCommentRequest request,
      HttpServletRequest httpRequest) {

    String ipAddress = extractClientIpAddress(httpRequest);
    CommentResponse response = commentCommandService.createComment(postId, request, ipAddress);

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @DeleteMapping("{commentId}")
  public ResponseEntity<Void> deleteComment(
      @PathVariable String commentId, @RequestBody @Valid DeleteCommentRequest request) {

    commentCommandService.deleteComment(commentId, request.getPassword());
    return ResponseEntity.noContent().build();
  }

  @PatchMapping("/{commentId}/review")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<CommentResponse> reviewComment(
      @PathVariable String commentId, @RequestBody @Valid CommentReviewRequest request) {

    CommentResponse response = commentCommandService.reviewComment(commentId, request);
    return ResponseEntity.ok(response);
  }

  private String extractClientIpAddress(HttpServletRequest request) {
    String xForwardedFor = request.getHeader("X-Forwarded-For");
    if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
      return xForwardedFor.split(",")[0].trim();
    }
    return request.getRemoteAddr();
  }
}
