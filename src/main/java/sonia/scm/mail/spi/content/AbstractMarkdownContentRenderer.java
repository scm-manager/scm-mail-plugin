package sonia.scm.mail.spi.content;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import sonia.scm.template.TemplateEngineFactory;

abstract class AbstractMarkdownContentRenderer extends AbstractTemplateContentRenderer {

  private static final Parser MARKDOWN_PARSER = Parser.builder().build();

  AbstractMarkdownContentRenderer(TemplateEngineFactory templateEngineFactory) {
    super(templateEngineFactory);
  }

  @Override
  public MailContent render(Context context) {
    String markdown = renderAsString(context);
    Node node = MARKDOWN_PARSER.parse(markdown);
    return render(node);
  }

  abstract MailContent render(Node node);
}
