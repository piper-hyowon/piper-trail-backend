package com.piper_trail.blog.shared.util;

import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.regex.Pattern;

@Component
public class SlugGenerator {

  private static final Pattern NONLATIN = Pattern.compile("[^\\w가-힣-]");
  private static final Pattern WHITESPACE = Pattern.compile("\\s");

  public String generateSlug(String title) {
    if (title == null || title.trim().isEmpty()) {
      return "untitled";
    }

    String nowhitespace = WHITESPACE.matcher(title).replaceAll("-");
    String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFKC);
    String slug = NONLATIN.matcher(normalized).replaceAll("");

    slug = slug.toLowerCase().replaceAll("-+", "-").replaceAll("^-|-$", "");

    return slug.isEmpty() ? "untitled" : slug;
  }
}
