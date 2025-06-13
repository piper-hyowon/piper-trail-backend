package com.piper_trail.blog.shared.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {

  private final LanguageArgumentResolver languageArgumentResolver;

  public WebConfig(LanguageArgumentResolver languageArgumentResolver) {
    this.languageArgumentResolver = languageArgumentResolver;
  }

  @Override
  public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
    resolvers.add(languageArgumentResolver);
  }
}
