package com.piper_trail.blog.query.series;

import com.piper_trail.blog.shared.domain.Post;
import com.piper_trail.blog.shared.domain.PostRepository;
import com.piper_trail.blog.shared.domain.Series;
import com.piper_trail.blog.shared.domain.SeriesRepository;
import com.piper_trail.blog.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SeriesQueryService {
  private final SeriesRepository seriesRepository;
  private final PostRepository postRepository;

  @Cacheable(value = "series", key = "'slug:' + #slug + ':' + #lang")
  public SeriesDetailResponse getSeriesBySlug(String slug, String lang) {
    Series series =
        seriesRepository
            .findBySlug(slug)
            .orElseThrow(() -> new ResourceNotFoundException("series", slug));

    List<Post> posts = postRepository.findBySeriesId(series.getId(), Sort.by("series.order"));

    boolean isEnglish = "en".equalsIgnoreCase(lang);
    String category = posts.isEmpty() ? null : posts.get(0).getCategory();

    List<SeriesDetailResponse.SeriesPostItem> postItems =
        posts.stream()
            .map(
                post ->
                    SeriesDetailResponse.SeriesPostItem.builder()
                        .id(post.getId())
                        .slug(post.getSlug())
                        .title(
                            isEnglish && post.getTitleEn() != null
                                ? post.getTitleEn()
                                : post.getTitle())
                        .titleEn(post.getTitleEn())
                        .subtitle(
                            isEnglish && post.getSubtitleEn() != null
                                ? post.getSubtitleEn()
                                : post.getSubtitle())
                        .subtitleEn(post.getSubtitleEn())
                        .order(post.getSeries().getOrder())
                        .createdAt(post.getCreatedAt())
                        .viewCount(post.getViewCount())
                        .build())
            .collect(Collectors.toList());

    return SeriesDetailResponse.builder()
        .id(series.getId())
        .slug(series.getSlug())
        .category(category)
        .title(isEnglish && series.getTitleEn() != null ? series.getTitleEn() : series.getTitle())
        .titleEn(series.getTitleEn())
        .description(
            isEnglish && series.getDescriptionEn() != null
                ? series.getDescriptionEn()
                : series.getDescription())
        .descriptionEn(series.getDescriptionEn())
        .totalCount(series.getTotalCount())
        .tags(series.getTags())
        .createdAt(series.getCreatedAt())
        .lastUpdated(series.getUpdatedAt())
        .posts(postItems)
        .build();
  }
}
