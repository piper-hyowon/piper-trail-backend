package com.piper_trail.blog.command.postcard;

import com.piper_trail.blog.shared.domain.Postcard;
import com.piper_trail.blog.shared.domain.PostcardRepository;
import com.piper_trail.blog.shared.exception.RateLimitExceededException;
import com.piper_trail.blog.shared.util.ClientIpUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostcardCommandService {

  private final PostcardRepository postcardRepository;
  private final PostcardRateLimiter postcardRateLimiter;

  public Postcard createPostcard(CreatePostcardRequest request, HttpServletRequest httpRequest) {
    String ipAddress = ClientIpUtils.extractClientIp(httpRequest);
    if (!postcardRateLimiter.isAllowed(ipAddress)) {
      throw new RateLimitExceededException("너무 잦은 엽서");
    }

    Postcard postcard =
        Postcard.builder()
            .stampType(request.getStampType())
            .nickname(request.getNickname())
            .message(request.getMessage())
            .ipAddress(ClientIpUtils.extractClientIp(httpRequest))
            .userAgent(httpRequest.getHeader("User-Agent"))
            .build();

    return postcardRepository.save(postcard);
  }
}
