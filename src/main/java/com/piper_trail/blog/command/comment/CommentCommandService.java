package com.piper_trail.blog.command.comment;

import com.piper_trail.blog.shared.domain.Comment;
import com.piper_trail.blog.shared.domain.CommentRepository;
import com.piper_trail.blog.shared.domain.PostRepository;
import com.piper_trail.blog.shared.event.CommentCreatedEvent;
import com.piper_trail.blog.shared.event.EventPublisher;
import com.piper_trail.blog.shared.exception.ResourceNotFoundException;
import com.piper_trail.blog.shared.exception.VisitorInvalidPasswordException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentCommandService {

  private final PostRepository postRepository;
  private final CommentRepository commentRepository;
  private final SpamDetectionService spamDetectionService;
  private final PasswordEncoder passwordEncoder;
  private final EventPublisher eventPublisher;

  @Transactional
  public CommentResponse createComment(
      String postId, CreateCommentRequest request, String ipAddress) {

    postRepository
        .findById(postId)
        .orElseThrow(() -> new ResourceNotFoundException("post", postId));

    Instant oneHourAgo = Instant.now().minus(1, ChronoUnit.HOURS);
    long recentCommentCount = commentRepository.countRecentByIpAddress(ipAddress, oneHourAgo);
    double riskScore =
        spamDetectionService.calculateRiskScore(
            request.getContent(), request.getAuthor(), ipAddress, recentCommentCount);

    boolean shouldHide = spamDetectionService.shouldHide(riskScore);
    boolean needsReview = spamDetectionService.needsReview(riskScore);

    Comment comment =
        Comment.builder()
            .postId(postId)
            .author(request.getAuthor())
            .content(request.getContent())
            .passwordHash(passwordEncoder.encode(request.getPassword()))
            .fontFamily(request.getFontFamily())
            .textColor(request.getTextColor())
            .ipAddress(ipAddress)
            .approved(true)
            .hidden(shouldHide)
            .riskScore(riskScore)
            .needsReview(needsReview)
            .build();

    Comment savedComment = commentRepository.save(comment);

    CommentCreatedEvent event =
        new CommentCreatedEvent(
            savedComment.getId(),
            postId,
            savedComment.getAuthor(),
            savedComment.getContent(),
            riskScore);
    eventPublisher.publish(event);

    if (needsReview) {
      // TODO: email 발송?
      log.warn("Comment requires review - ID: {}, Risk Score: {}", savedComment.getId(), riskScore);
    }

    return convertToResponse(savedComment);
  }

  @Transactional
  public void deleteComment(String commentId, String password) {
    Comment comment =
        commentRepository
            .findById(commentId)
            .orElseThrow(() -> new ResourceNotFoundException("comment", commentId));

    if (!passwordEncoder.matches(password, comment.getPasswordHash())) {
      throw new VisitorInvalidPasswordException(
          VisitorInvalidPasswordException.VisitorAction.Comment);
    }

    commentRepository.delete(comment);
  }

  @Transactional
  public CommentResponse reviewComment(String commentId, CommentReviewRequest request) {
    Comment comment =
        commentRepository
            .findById(commentId)
            .orElseThrow(() -> new ResourceNotFoundException("comment", commentId));

    comment.setApproved(request.getApproved());
    comment.setNeedsReview(false);
    comment.setReviewReason(request.getReason());
    comment.setReviewedAt(Instant.now());

    if (!request.getApproved()) {
      comment.setHidden(true);
    }

    return convertToResponse(commentRepository.save(comment));
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
}
