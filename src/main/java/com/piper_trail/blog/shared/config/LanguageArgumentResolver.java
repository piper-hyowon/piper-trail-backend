package com.piper_trail.blog.shared.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.Set;

@Component
public class LanguageArgumentResolver implements HandlerMethodArgumentResolver {

  private static final Set<String> SUPPORTED_LANGUAGES = Set.of("ko", "en");
  private static final String DEFAULT_LANGUAGE = "ko";

  @Override
  public boolean supportsParameter(MethodParameter parameter) {
    return parameter.getParameterType().equals(String.class)
        && parameter.hasParameterAnnotation(Language.class);
  }

  @Override
  @NonNull
  public Object resolveArgument(
      @NonNull MethodParameter parameter,
      @Nullable ModelAndViewContainer mavContainer,
      @NonNull NativeWebRequest webRequest,
      @Nullable WebDataBinderFactory binderFactory) {
    HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);

    if (request == null) {
      return DEFAULT_LANGUAGE;
    }

    String lang = request.getParameter("lang");
    if (lang != null && SUPPORTED_LANGUAGES.contains(lang.toLowerCase())) {
      return lang.toLowerCase();
    }

    // Accept-Language 헤더
    String acceptLanguage = request.getHeader("Accept-Language");
    if (acceptLanguage != null) {
      // "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7" 형식 파싱
      String[] languages = acceptLanguage.split(",");
      for (String language : languages) {
        String[] parts = language.split(";");
        if (parts.length > 0) {
          String[] langParts = parts[0].split("-");
          if (langParts.length > 0) {
            String code = langParts[0].trim().toLowerCase();
            if (SUPPORTED_LANGUAGES.contains(code)) {
              return code;
            }
          }
        }
      }
    }

    Language annotation = parameter.getParameterAnnotation(Language.class);
    return annotation != null ? annotation.defaultValue() : DEFAULT_LANGUAGE;
  }
}
