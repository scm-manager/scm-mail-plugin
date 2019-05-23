package sonia.scm.mail.spi.content;

import org.mockito.Mock;
import sonia.scm.template.Template;
import sonia.scm.template.TemplateEngine;
import sonia.scm.template.TemplateEngineFactory;

import java.io.IOException;
import java.util.Locale;

import static org.mockito.Mockito.when;

class TemplateTestBase {

  @Mock
  TemplateEngineFactory templateEngineFactory;

  @Mock
  TemplateEngine templateEngine;

  Template template = (writer, model) -> writer.write(model.toString() + "!");

  ContentRenderer.Context prepareEngineAndCreateContext(String templatePath, Locale locale, Object model) throws IOException {
    when(templateEngineFactory.getEngineByExtension(templatePath)).thenReturn(templateEngine);
    when(templateEngine.getTemplate(templatePath, locale)).thenReturn(template);
    return new ContentRenderer.Context(templatePath, locale, model);
  }

}
