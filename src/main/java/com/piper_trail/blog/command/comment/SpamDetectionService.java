package com.piper_trail.blog.command.comment;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@ConfigurationProperties(prefix = "comment.spam")
@Data
public class SpamDetectionService {

  // TODO: Google Perspective API 연동
  private List<String> keywords;
  private int maxCommentsPerHour;
  private double hideThreshold;
  private double reviewThreshold;

  public double calculateRiskScore(
      String content, String author, String ipAddress, long recentCommentCount) {
    // author, ipAddress 추후 활용 예정

    double riskScore = 0.0;

    // 욕설/스팸 키워드 체크
    String lowerContent = content.toLowerCase();
    for (String keyword : keywords) {
      if (lowerContent.contains(keyword)) {
        riskScore += 0.3;
      }
    }

    // 잦은 스팸성 댓글
    if (recentCommentCount >= maxCommentsPerHour) {
      riskScore += 0.4;
    }

    // 특수문자 남발
    long specialCharCount = content.chars().filter(ch -> "!@#$%^&*()".indexOf(ch) != -1).count();

    if (specialCharCount > content.length() * 0.3) {
      riskScore += 0.2;
    }

    // 텍스트 반복
    if (content.length() > 10) {
      String firstHalf = content.substring(0, content.length() / 2);
      String secondHalf = content.substring(content.length() / 2);
      if (firstHalf.equals(secondHalf)) {
        riskScore += 0.3;
      }
    }

    return Math.min(riskScore, 1.0); // 최대 1.0
  }

  public boolean shouldHide(double riskScore) {
    return riskScore >= 0.7;
  }

  public boolean needsReview(double riskScore) {
    return riskScore >= 0.4;
  }
}
