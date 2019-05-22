package sonia.scm.mail.spi.content;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class TextContentRendererTest extends TemplateTestBase {

  @InjectMocks
  private TextContentRenderer renderer;

  @Test
  void shouldReturnRenderedText() throws IOException {
    ContentRenderer.Context context = prepareEngineAndCreateContext("/tpl.mustache", Locale.GERMAN, "super");

    MailContent awesome = renderer.render(context);
    assertThat(awesome.getText()).isEqualTo("super!");
  }

}
