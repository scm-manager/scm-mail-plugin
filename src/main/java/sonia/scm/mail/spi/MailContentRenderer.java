package sonia.scm.mail.spi;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.Renderer;
import org.commonmark.renderer.html.HtmlRenderer;
import org.commonmark.renderer.text.TextContentRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.template.Template;
import sonia.scm.template.TemplateEngine;
import sonia.scm.template.TemplateEngineFactory;
import sonia.scm.mail.api.MailTemplateType;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Locale;

public class MailContentRenderer {

  private static final Logger LOG = LoggerFactory.getLogger(MailContentRenderer.class);

  private static final Parser MARKDOWN_PARSER = Parser.builder().build();
  private static final Renderer HTML_RENDERER = HtmlRenderer.builder().build();
  private static final Renderer TEXT_RENDERER = TextContentRenderer.builder().build();

  private final TemplateEngineFactory templateEngineFactory;
  private final String templatePath;
  private final MailTemplateType templateType;

  MailContentRenderer(TemplateEngineFactory templateEngineFactory, String templatePath, MailTemplateType templateType) {
    this.templateEngineFactory = templateEngineFactory;
    this.templatePath = templatePath;
    this.templateType = templateType;
  }

  MailContent createMailContent(Locale preferredLocale, Object templateModel) throws IOException {
    TemplateEngine templateEngine = templateEngineFactory.getEngineByExtension(templatePath);

    LOG.trace("trying to load template path {} with preferred locale {}", templatePath, preferredLocale);
    Template template = templateEngine.getTemplate(templatePath, preferredLocale);

    String content = getMailContent(template, templateModel);
    if (templateType.isMarkdown()) {
      Node markdownContent = MARKDOWN_PARSER.parse(content);
      String textContent = TEXT_RENDERER.render(markdownContent);
      if (templateType.isHtml()) {
        String htmlContent = HTML_RENDERER.render(markdownContent);
        return MailContent.textAndHtml(textContent, htmlContent);
      } else {
        return MailContent.text(textContent);
      }

    } else if (templateType.isHtml()) {
      return MailContent.html(content);
    } else {
      return MailContent.text(content);
    }
  }

  private String getMailContent(Template template, Object templateModel) throws IOException {
    StringWriter writer = new StringWriter();
    template.execute(writer, templateModel);
    return writer.toString();
  }
}
