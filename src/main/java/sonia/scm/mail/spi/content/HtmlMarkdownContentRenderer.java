package sonia.scm.mail.spi.content;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import org.commonmark.node.Node;
import org.commonmark.renderer.Renderer;
import org.commonmark.renderer.html.HtmlRenderer;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.template.Template;
import sonia.scm.template.TemplateEngineFactory;

import javax.inject.Inject;
import java.util.Locale;

class HtmlMarkdownContentRenderer extends AbstractMarkdownContentRenderer {

  @VisibleForTesting
  static final String TEMPLATE_PATH = "/sonia/scm/mail/layout.mustache";

  private static final Renderer HTML_RENDERER = HtmlRenderer.builder()
    .attributeProviderFactory(c -> new HtmlMailAttributeProvider())
    .build();

  private final TextMarkdownContentRenderer textRenderer;
  private final ScmConfiguration configuration;
  private final Template layoutTemplate;

  @Inject
  HtmlMarkdownContentRenderer(TemplateEngineFactory templateEngineFactory, TextMarkdownContentRenderer textRenderer, ScmConfiguration configuration) {
    super(templateEngineFactory);
    this.textRenderer = textRenderer;
    this.configuration = configuration;
    this.layoutTemplate = readTemplate(TEMPLATE_PATH, Locale.ENGLISH);
  }

  @Override
  protected MailContent render(Node node) {
    String text = textRenderer.renderAsString(node);
    String html = renderHtml(node);
    return MailContent.textAndHtml(text, html);
  }

  private String renderHtml(Node node) {
    String html = HTML_RENDERER.render(node);
    return renderTemplate(layoutTemplate, new LayoutModel(html, configuration.getBaseUrl()));
  }

  private final class LayoutModel {

    private String content;
    private String url;

    private LayoutModel(String content, String url) {
      this.content = content;
      this.url = url;
    }

    public String getContent() {
      return content;
    }

    public String getUrl() {
      return url;
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this)
        .add("content", content)
        .add("url", url)
        .toString();
    }
  }
}
