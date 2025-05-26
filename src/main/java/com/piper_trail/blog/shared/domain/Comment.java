package com.piper_trail.blog.shared.domain;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "comments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Comment {
  public enum FontFamily {
    DEFAULT,
    SERIF,
    SANS_SERIF,
    MONOSPACE
  }

  public enum TextColor {
    DEFAULT,
    BLACK,
    BLUE,
    RED,
    GREEN
  }

  @Id private String id;

  @Indexed private String postId;

  @Size(max = 1000)
  private String content;

  @Size(max = 20)
  private String author;

  private String passwordHash;

  @Builder.Default private FontFamily fontFamily = FontFamily.DEFAULT;
  @Builder.Default private TextColor textColor = TextColor.DEFAULT;

  @Indexed private String ipAddress; // 스팸 방지, 관리용

  @Builder.Default private boolean approved = true; // 기본 승인
  private String reviewReason;
  private Instant reviewedAt;
  @Builder.Default private boolean hidden = false;

  @Builder.Default
  @DecimalMin("0.0")
  @DecimalMax("1.0")
  private double riskScore = 0.0;

  @Builder.Default private boolean needsReview = false; // 관리자 검토 필요

  @CreatedDate private Instant createdAt;

  @LastModifiedDate private Instant updatedAt;
}
