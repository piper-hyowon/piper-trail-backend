package com.piper_trail.blog.command.series;

import com.piper_trail.blog.query.series.SeriesResponse;
import com.piper_trail.blog.shared.domain.Series;
import com.piper_trail.blog.shared.domain.SeriesRepository;
import com.piper_trail.blog.shared.event.EventPublisher;
import com.piper_trail.blog.shared.event.SeriesCreatedEvent;
import com.piper_trail.blog.shared.event.SeriesUpdatedEvent;
import com.piper_trail.blog.shared.exception.ResourceNotFoundException;
import com.piper_trail.blog.shared.util.SlugGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SeriesCommandService {
  private final SeriesRepository seriesRepository;
  private final EventPublisher eventPublisher;
  private final SlugGenerator slugGenerator;

  @Transactional
  public SeriesResponse createSeries(CreateSeriesRequest request) {
    String uniqueSlug = ensureUniqueSlug(slugGenerator.generateSlug(request.getTitle()));

    Series series =
        Series.builder()
            .slug(uniqueSlug)
            .title(request.getTitle())
            .titleEn(request.getTitleEn())
            .description(request.getDescription())
            .descriptionEn(request.getDescriptionEn())
            .tags(request.getTags())
            .totalCount(0)
            .build();

    Series savedSeries = seriesRepository.save(series);

    SeriesCreatedEvent event =
        new SeriesCreatedEvent(
            savedSeries.getId(),
            savedSeries.getTitle(),
            savedSeries.getSlug(),
            savedSeries.getDescription(),
            savedSeries.getTags());
    eventPublisher.publish(event);

    return convertToResponse(savedSeries);
  }

  @Transactional
  public SeriesResponse updateSeries(String id, UpdateSeriesRequest request) {
    Series series =
        seriesRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("series", id));

    series.setTitle(request.getTitle());
    series.setTitleEn(request.getTitleEn());
    series.setDescription(request.getDescription());
    series.setDescriptionEn(request.getDescriptionEn());
    series.setTags(request.getTags());

    Series updatedSeries = seriesRepository.save(series);

    SeriesUpdatedEvent event =
        new SeriesUpdatedEvent(
            updatedSeries.getId(),
            updatedSeries.getTitle(),
            updatedSeries.getSlug(),
            updatedSeries.getTotalCount());
    eventPublisher.publish(event);

    return convertToResponse(updatedSeries);
  }

  private String ensureUniqueSlug(String baseSlug) {
    String candidateSlug = baseSlug;
    int counter = 1;

    while (seriesRepository.existsBySlug(candidateSlug)) {
      candidateSlug = baseSlug + "-" + counter;
      counter++;
    }

    return candidateSlug;
  }

  private SeriesResponse convertToResponse(Series series) {
    return SeriesResponse.builder()
        .id(series.getId())
        .slug(series.getSlug())
        .title(series.getTitle())
        .titleEn(series.getTitleEn())
        .description(series.getDescription())
        .descriptionEn(series.getDescriptionEn())
        .totalCount(series.getTotalCount())
        .tags(series.getTags())
        .createdAt(series.getCreatedAt())
        .lastUpdated(series.getUpdatedAt())
        .build();
  }
}
