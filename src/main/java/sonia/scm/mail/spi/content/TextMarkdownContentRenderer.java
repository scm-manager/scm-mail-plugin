package sonia.scm.mail.spi.content;

import org.commonmark.node.Node;
import org.commonmark.renderer.Renderer;
import org.commonmark.renderer.text.TextContentRenderer;
import sonia.scm.template.TemplateEngineFactory;

import javax.inject.Inject;

class TextMarkdownContentRenderer extends AbstractMarkdownContentRenderer {

  private static final Renderer TEXT_RENDERER = TextContentRenderer.builder().build();

  @Inject
  TextMarkdownContentRenderer(TemplateEngineFactory templateEngineFactory) {
    super(templateEngineFactory);
  }

  String renderAsString(Node node) {
    return TEXT_RENDERER.render(node);
  }

  @Override
  protected MailContent render(Node node) {
    return MailContent.text(renderAsString(node));
  }
}
