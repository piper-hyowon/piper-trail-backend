package com.piper_trail.blog.query.series;

import com.piper_trail.blog.shared.config.Language;
import com.piper_trail.blog.shared.util.ETagGenerator;
import com.piper_trail.blog.shared.util.HttpCacheUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/series")
@RequiredArgsConstructor
public class SeriesQueryController {
  private final SeriesQueryService seriesQueryService;
  private final ETagGenerator etagGenerator;

  @GetMapping("/{slug}")
  public ResponseEntity<SeriesDetailResponse> getSeriesBySlug(
      @PathVariable String slug,
      @Language String language,
      @RequestHeader(value = "If-None-Match", required = false) String ifNoneMatch) {

    SeriesDetailResponse response = seriesQueryService.getSeriesBySlug(slug, language);

    String etag = etagGenerator.generateETag("series", slug, response.getLastUpdated());

    if (HttpCacheUtils.isETagMatched(etag, ifNoneMatch)) {
      return HttpCacheUtils.createNotModifiedResponse(etag, HttpCacheUtils.POST_DETAIL_CACHE);
    }

    return HttpCacheUtils.createCachedResponse(
        response, etag, HttpCacheUtils.POST_DETAIL_CACHE, response.getLastUpdated());
  }
}
