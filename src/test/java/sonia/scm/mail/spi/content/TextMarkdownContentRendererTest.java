package sonia.scm.mail.spi.content;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class TextMarkdownContentRendererTest extends TemplateTestBase {

  @InjectMocks
  private TextMarkdownContentRenderer renderer;

  @Test
  void shouldRenderMarkdownAndReturnText() throws IOException {
    ContentRenderer.Context context = prepareEngineAndCreateContext("/tpl.mustache", Locale.ENGLISH, "# Awesome");
    MailContent content = renderer.render(context);
    assertThat(content.getText()).isEqualTo("Awesome!");
  }

}
