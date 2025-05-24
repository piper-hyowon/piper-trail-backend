package com.piper_trail.blog.command.post;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
@Slf4j
public class PostCommandController {

  private final PostCommandService postCommandService;

  @PostMapping
  public ResponseEntity<PostResponse> createPost(@Valid @RequestBody CreatePostRequest request) {

    PostResponse response = postCommandService.createPost(request);

    URI location = URI.create("/posts/" + response.getSlug());

    return ResponseEntity.created(location).body(response);
  }

  @PutMapping("/{id}")
  public ResponseEntity<PostResponse> updatePost(
      @PathVariable String id, @Valid @RequestBody UpdatePostRequest request) {

    PostResponse response = postCommandService.updatePost(id, request);

    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deletePost(@PathVariable String id) {

    postCommandService.deletePost(id);

    return ResponseEntity.noContent().build();
  }
}
