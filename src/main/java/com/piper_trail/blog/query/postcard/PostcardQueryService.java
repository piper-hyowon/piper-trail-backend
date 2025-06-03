package com.piper_trail.blog.query.postcard;

import com.piper_trail.blog.shared.domain.Postcard;
import com.piper_trail.blog.shared.domain.PostcardRepository;
import com.piper_trail.blog.shared.dto.PagedResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostcardQueryService {

  private final PostcardRepository postcardRepository;

  public PagedResponse<PostcardResponse> getPostcards(int page, int size) {
    size = Math.min(size, 50);

    Pageable pageable = PageRequest.of(page, size);
    Page<Postcard> postcardPage = postcardRepository.findAllByOrderByCreatedAtDesc(pageable);

    List<PostcardResponse> responses =
        postcardPage.getContent().stream().map(this::toResponse).toList();

    return PagedResponse.<PostcardResponse>builder()
        .content(responses)
        .page(page)
        .size(size)
        .total(postcardPage.getTotalElements())
        .build();
  }

  private PostcardResponse toResponse(Postcard postcard) {
    return PostcardResponse.builder()
        .id(postcard.getId())
        .stampType(postcard.getStampType())
        .nickname(postcard.getNickname())
        .message(postcard.getMessage())
        .createdAt(postcard.getCreatedAt())
        .build();
  }
}
