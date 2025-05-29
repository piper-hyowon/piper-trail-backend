package com.piper_trail.blog.command.postcard;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/postcards")
@RequiredArgsConstructor
public class PostcardCommandController {

  private final PostcardCommandService postcardCommandService;

  @PostMapping
  public ResponseEntity<?> createPostcard(
      @Valid @RequestBody CreatePostcardRequest request, HttpServletRequest httpRequest) {
    return ResponseEntity.created(URI.create("/postcards/"))
        .body(postcardCommandService.createPostcard(request, httpRequest));
  }
}
