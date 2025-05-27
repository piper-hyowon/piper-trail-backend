package com.piper_trail.blog.query.comment;

import com.piper_trail.blog.command.comment.CommentResponse;
import com.piper_trail.blog.shared.domain.Comment;
import com.piper_trail.blog.shared.domain.CommentRepository;
import com.piper_trail.blog.shared.dto.PagedResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentQueryService {
  private final CommentRepository commentRepository;

  public PagedResponse<CommentResponse> getCommentsByPostId(String postId, Pageable pageable) {
    Page<Comment> commentPage = commentRepository.findByPostId(postId, pageable);
    return convertToPagedResponse(commentPage);
  }

  public List<CommentAdminResponse> getAllComments() {
    return commentRepository.findAll().stream()
        .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
        .map(this::convertToAdminResponse)
        .collect(Collectors.toList());
  }

  private PagedResponse<CommentResponse> convertToPagedResponse(Page<Comment> commentPage) {
    List<CommentResponse> content =
        commentPage.getContent().stream().map(this::convertToResponse).collect(Collectors.toList());

    return PagedResponse.<CommentResponse>builder()
        .content(content)
        .page(commentPage.getNumber())
        .size(commentPage.getSize())
        .total(commentPage.getTotalElements())
        .build();
  }

  private CommentResponse convertToResponse(Comment comment) {
    return CommentResponse.builder()
        .id(comment.getId())
        .author(comment.getAuthor())
        .content(comment.isNeedsReview() ? "검토 중입니다." : comment.getContent())
        .fontFamily(comment.isNeedsReview() ? Comment.FontFamily.DEFAULT : comment.getFontFamily())
        .textColor(comment.isNeedsReview() ? Comment.TextColor.DEFAULT : comment.getTextColor())
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
