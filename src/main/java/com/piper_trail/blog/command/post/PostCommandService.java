package com.piper_trail.blog.command.post;

import com.piper_trail.blog.shared.domain.Post;
import com.piper_trail.blog.shared.domain.PostRepository;
import com.piper_trail.blog.shared.domain.Series;
import com.piper_trail.blog.shared.domain.SeriesRepository;
import com.piper_trail.blog.shared.event.*;
import com.piper_trail.blog.shared.exception.ResourceNotFoundException;
import com.piper_trail.blog.shared.util.SlugGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostCommandService {

  private final PostRepository postRepository;
  private final SeriesRepository seriesRepository;
  private final EventPublisher eventPublisher;
  private final SlugGenerator slugGenerator;

  @Transactional
  public PostResponse createPost(CreatePostRequest request) {
    String uniqueSlug = ensureUniqueSlug(slugGenerator.generateSlug(request.getTitle()));

    Post.PostBuilder postBuilder =
        Post.builder()
            .title(request.getTitle())
            .subtitle(request.getSubtitle())
            .slug(uniqueSlug)
            .markdownContent(request.getMarkdownContent())
            .titleEn(request.getTitleEn())
            .subtitleEn(request.getSubtitleEn())
            .markdownContentEn(request.getMarkdownContentEn())
            .category(request.getCategory())
            .tags(request.getTags())
            .viewCount(0);

    // 시리즈 글인 경우
    if (request.getSeriesId() != null) {
      Series series =
          seriesRepository
              .findById(request.getSeriesId())
              .orElseThrow(() -> new ResourceNotFoundException("series", request.getSeriesId()));

      int order;
      if (request.getSeriesOrder() != null) {
        order = request.getSeriesOrder();
        if (postRepository.existsBySeriesIdAndOrder(series.getId(), order)) {
          throw new IllegalArgumentException("Series order " + order + " already exists");
        }
      } else {
        order = series.getTotalCount() + 1;
      }

      postBuilder.isSeries(true);
      postBuilder.series(
          Post.SeriesInfo.builder()
              .seriesId(series.getId())
              .seriesTitle(series.getTitle())
              .order(order)
              .build());

      series.setTotalCount(series.getTotalCount() + 1);
      seriesRepository.save(series);
    }

    Post savedPost = postRepository.save(postBuilder.build());

    PostCreatedEvent event =
        new PostCreatedEvent(
            savedPost.getId(),
            savedPost.getTitle(),
            savedPost.getSlug(),
            savedPost.getCategory(),
            savedPost.getTags());
    eventPublisher.publish(event);

    if (savedPost.isSeries()) {
      SeriesPostAddedEvent seriesEvent =
          new SeriesPostAddedEvent(
              savedPost.getId(),
              savedPost.getSeries().getSeriesId(),
              savedPost.getTitle(),
              savedPost.getSlug(),
              savedPost.getSeries().getOrder(),
              savedPost.getViewCount(),
              savedPost.getCreatedAt());
      eventPublisher.publish(seriesEvent);
    }

    return convertToResponse(savedPost);
  }

  @Transactional
  public void deletePost(String id) {
    log.info("Deleting post: {}", id);

    Post existingPost =
        postRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Post Not Found: " + id));

    String title = existingPost.getTitle();
    String slug = existingPost.getSlug();
    String category = existingPost.getCategory();
    List<String> tags = existingPost.getTags();
    long viewCount = existingPost.getViewCount();

    postRepository.delete(existingPost);

    PostDeletedEvent event = new PostDeletedEvent(id, title, slug, category, tags, viewCount);
    eventPublisher.publish(event);
  }

  private String ensureUniqueSlug(String baseSlug) {
    return ensureUniqueSlug(baseSlug, null);
  }

  private String ensureUniqueSlug(String baseSlug, String excludeId) {
    String candidateSlug = baseSlug;
    int counter = 1;

    while (true) {
      boolean exists;
      if (excludeId != null) {
        exists = postRepository.existsBySlugAndIdNot(candidateSlug, excludeId);
      } else {
        exists = postRepository.existsBySlug(candidateSlug);
      }

      if (!exists) {
        return candidateSlug;
      }

      candidateSlug = baseSlug + "-" + counter;
      counter++;
    }
  }

  private PostResponse convertToResponse(Post post) {
    return PostResponse.builder()
        .id(post.getId())
        .title(post.getTitle())
        .slug(post.getSlug())
        .category(post.getCategory())
        .tags(post.getTags())
        .viewCount(post.getViewCount())
        .createdAt(post.getCreatedAt())
        .updatedAt(post.getUpdatedAt())
        .build();
  }
}
