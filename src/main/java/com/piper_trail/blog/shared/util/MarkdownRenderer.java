package com.piper_trail.blog.shared.util;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.MutableDataSet;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Service;

@Service
public class MarkdownRenderer {

  private final Parser parser;
  private final HtmlRenderer renderer;
  private final Safelist safelist;

  public MarkdownRenderer() {
    MutableDataSet options = new MutableDataSet();
    options.set(HtmlRenderer.SUPPRESS_HTML, true);

    this.parser = Parser.builder(options).build();
    this.renderer = HtmlRenderer.builder(options).build();
    this.safelist = Safelist.relaxed();
  }

  public String renderToHtml(String markdown) {
    if (markdown == null || markdown.trim().isEmpty()) {
      return "";
    }

    return Jsoup.clean(renderer.render(parser.parse(markdown)), safelist);
  }
}
