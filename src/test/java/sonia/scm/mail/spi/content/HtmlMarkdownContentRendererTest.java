package sonia.scm.mail.spi.content;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.template.Template;
import sonia.scm.template.TemplateEngine;
import sonia.scm.template.TemplateEngineFactory;

import java.io.IOException;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HtmlMarkdownContentRendererTest {

  @Mock
  private TemplateEngineFactory templateEngineFactory;

  @Mock
  private TemplateEngine templateEngine;

  private Template content = (writer, model) -> writer.write(model + "!");
  private Template layout = (writer, model) -> writer.write("<>" + model + "</>");

  private ScmConfiguration configuration;

  @BeforeEach
  void setUp() {
    configuration = new ScmConfiguration();
    configuration.setBaseUrl("http://hitchhiker.com");
  }

  @Test
  void shouldRenderHtmlAndText() throws IOException {
    when(templateEngineFactory.getEngineByExtension(anyString())).thenReturn(templateEngine);
    when(templateEngine.getTemplate(anyString(), any(Locale.class))).thenAnswer((ic) -> {
      String path = ic.getArgument(0);
      if (HtmlMarkdownContentRenderer.TEMPLATE_PATH.equals(path)) {
        return layout;
      } else if ("/tpl.mustache".equals(path)) {
        return content;
      } else {
        throw new MailContentException("not found");
      }
    });

    ContentRenderer.Context context = new ContentRenderer.Context("/tpl.mustache", Locale.ENGLISH, "# Super");

    HtmlMarkdownContentRenderer renderer = new HtmlMarkdownContentRenderer(
      templateEngineFactory, new TextMarkdownContentRenderer(templateEngineFactory), configuration
    );

    MailContent content = renderer.render(context);
    assertThat(content.getText()).isEqualTo("Super!");
    assertThat(content.getHtml())
      .startsWith("<>")
      .contains("<h1 style=")
      .contains("Super!")
      .contains("</h1>")
      .endsWith("</>");
  }
}
