package com.piper_trail.blog.command.post;

import com.piper_trail.blog.shared.domain.Post;
import com.piper_trail.blog.shared.domain.PostRepository;
import com.piper_trail.blog.shared.event.EventPublisher;
import com.piper_trail.blog.shared.event.PostCreatedEvent;
import com.piper_trail.blog.shared.event.PostDeletedEvent;
import com.piper_trail.blog.shared.event.PostUpdatedEvent;
import com.piper_trail.blog.shared.exception.ResourceNotFoundException;
import com.piper_trail.blog.shared.util.MarkdownRenderer;
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
  private final EventPublisher eventPublisher;
  private final SlugGenerator slugGenerator;
  private final MarkdownRenderer markdownRenderer;

  @Transactional
  public PostResponse createPost(CreatePostRequest request) {
    String uniqueSlug = ensureUniqueSlug(slugGenerator.generateSlug(request.getTitle()));

    Post post =
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
            .viewCount(0)
            .build();

    Post savedPost = postRepository.save(post);

    PostCreatedEvent event =
        new PostCreatedEvent(
            savedPost.getId(),
            savedPost.getTitle(),
            savedPost.getSlug(),
            savedPost.getCategory(),
            savedPost.getTags());
    eventPublisher.publish(event);

    return convertToResponse(savedPost);
  }

  @Transactional
  public PostResponse updatePost(String id, UpdatePostRequest request) {
    Post existingPost =
        postRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("post: " + id));

    String previousTitle = existingPost.getTitle();
    String previousSlug = existingPost.getSlug();

    String newSlug = existingPost.getSlug();
    if (!request.getTitle().equals(previousTitle)) {
      String baseSlug = slugGenerator.generateSlug(request.getTitle());
      newSlug = ensureUniqueSlug(baseSlug, id);
    }

    existingPost.setTitle(request.getTitle());
    existingPost.setTitleEn(request.getTitleEn());
    existingPost.setSubtitle(request.getSubtitle());
    existingPost.setSubtitleEn(request.getSubtitleEn());
    existingPost.setSlug(newSlug);
    existingPost.setMarkdownContent(request.getMarkdownContent());
    existingPost.setMarkdownContentEn(request.getMarkdownContentEn());
    existingPost.setCategory(request.getCategory());
    existingPost.setTags(request.getTags());

    Post updatedPost = postRepository.save(existingPost);

    eventPublisher.publish(
        new PostUpdatedEvent(
            updatedPost.getId(),
            updatedPost.getTitle(),
            updatedPost.getSlug(),
            updatedPost.getCategory(),
            updatedPost.getTags(),
            previousTitle,
            previousSlug));

    return convertToResponse(updatedPost);
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
