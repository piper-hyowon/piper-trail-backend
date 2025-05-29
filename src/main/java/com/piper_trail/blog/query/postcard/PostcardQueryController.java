package com.piper_trail.blog.query.postcard;

import com.piper_trail.blog.shared.dto.PagedResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/postcards")
@RequiredArgsConstructor
public class PostcardQueryController {

  private final PostcardQueryService postcardQueryService;

  @GetMapping
  public ResponseEntity<PagedResponse<PostcardResponse>> getPostcards(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
    return ResponseEntity.ok(postcardQueryService.getPostcards(page, size));
  }
}
