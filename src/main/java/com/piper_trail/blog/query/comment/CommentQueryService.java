package com.piper_trail.blog.query.comment;

import com.piper_trail.blog.command.comment.CommentResponse;
import com.piper_trail.blog.shared.domain.Comment;
import com.piper_trail.blog.shared.domain.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentQueryService {
  private final CommentRepository commentRepository;

  public List<CommentResponse> getCommentsByPostId(String postId) {
    return commentRepository.findVisibleByPostId(postId).stream()
        .map(this::convertToResponse)
        .collect(Collectors.toList());
  }

  public List<CommentAdminResponse> getAllComments() {
    return commentRepository.findAll().stream()
        .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
        .map(this::convertToAdminResponse)
        .collect(Collectors.toList());
  }

  private CommentResponse convertToResponse(Comment comment) {
    return CommentResponse.builder()
        .id(comment.getId())
        .author(comment.getAuthor())
        .content(comment.getContent())
        .fontFamily(comment.getFontFamily())
        .textColor(comment.getTextColor())
        .createdAt(comment.getCreatedAt())
        .reviewNeeded(comment.isNeedsReview())
        .build();
  }

  private CommentAdminResponse convertToAdminResponse(Comment comment) {
    return CommentAdminResponse.builder()
        .id(comment.getId())
        .postId(comment.getPostId())
        .author(comment.getAuthor())
        .content(comment.getContent())
        .fontFamily(comment.getFontFamily())
        .textColor(comment.getTextColor())
        .ipAddress(comment.getIpAddress())
        .approved(comment.isApproved())
        .hidden(comment.isHidden())
        .needsReview(comment.isNeedsReview())
        .riskScore(comment.getRiskScore())
        .reviewReason(comment.getReviewReason())
        .reviewedAt(comment.getReviewedAt())
        .createdAt(comment.getCreatedAt())
        .updatedAt(comment.getUpdatedAt())
        .build();
  }
}
