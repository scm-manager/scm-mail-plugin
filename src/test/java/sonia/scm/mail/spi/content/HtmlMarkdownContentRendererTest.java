/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

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

  private final Template content = (writer, model) -> writer.write(model + "!");
  private final Template layout = (writer, model) -> writer.write("<>" + model + "</>");

  private ScmConfiguration configuration;

  @BeforeEach
  void setUp() throws IOException {
    configuration = new ScmConfiguration();
    configuration.setBaseUrl("http://hitchhiker.com");

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
  }

  @Test
  void shouldRenderHtmlAndText() {
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

  @Test
  void shouldRenderImageWithoutAltText() {
    ContentRenderer.Context context = new ContentRenderer.Context("/tpl.mustache", Locale.ENGLISH, "![](https://some-domain.de/some/image/url.png)");
    HtmlMarkdownContentRenderer renderer = new HtmlMarkdownContentRenderer(
      templateEngineFactory, new TextMarkdownContentRenderer(templateEngineFactory), configuration
    );

    MailContent content = renderer.render(context);
    assertThat(content.getText()).isEqualTo("\"[Image]\" (https://some-domain.de/some/image/url.png)!");
    assertThat(content.getHtml())
      .contains("<img src=\"https://some-domain.de/some/image/url.png\" alt=\"[Image]\" style=\"margin: 20px auto; padding: 0; font-size: 100%; font-family: BlinkMacSystemFont,-apple-system,Segoe UI,Roboto,Oxygen,Ubuntu,Cantarell,Fira Sans,Droid Sans,Helvetica Neue,Helvetica,Arial,sans-serif; line-height: 1.65; max-width: 100%; display: block;\" />");
  }

  @Test
  void shouldRenderImageWithAltText() {
    ContentRenderer.Context context = new ContentRenderer.Context("/tpl.mustache", Locale.ENGLISH, "![Some Alt Text](https://some-domain.de/some/image/url.png)");
    HtmlMarkdownContentRenderer renderer = new HtmlMarkdownContentRenderer(
      templateEngineFactory, new TextMarkdownContentRenderer(templateEngineFactory), configuration
    );

    MailContent content = renderer.render(context);
    assertThat(content.getText()).isEqualTo("\"Some Alt Text\" (https://some-domain.de/some/image/url.png)!");
    assertThat(content.getHtml())
      .contains("<img src=\"https://some-domain.de/some/image/url.png\" alt=\"Some Alt Text\" style=\"margin: 20px auto; padding: 0; font-size: 100%; font-family: BlinkMacSystemFont,-apple-system,Segoe UI,Roboto,Oxygen,Ubuntu,Cantarell,Fira Sans,Droid Sans,Helvetica Neue,Helvetica,Arial,sans-serif; line-height: 1.65; max-width: 100%; display: block;\" />");
  }

  @Test
  void shouldNotRenderRelativeImagePath() {
    ContentRenderer.Context context = new ContentRenderer.Context("/tpl.mustache", Locale.ENGLISH, "![Some Alt Text](/some/image/url.png)");
    HtmlMarkdownContentRenderer renderer = new HtmlMarkdownContentRenderer(
      templateEngineFactory, new TextMarkdownContentRenderer(templateEngineFactory), configuration
    );

    MailContent content = renderer.render(context);
    assertThat(content.getText()).isEqualTo("\"Some Alt Text\" (/some/image/url.png)!");
    assertThat(content.getHtml())
      .contains("<img src=\"\" alt=\"Some Alt Text\" style=\"margin: 20px auto; padding: 0; font-size: 100%; font-family: BlinkMacSystemFont,-apple-system,Segoe UI,Roboto,Oxygen,Ubuntu,Cantarell,Fira Sans,Droid Sans,Helvetica Neue,Helvetica,Arial,sans-serif; line-height: 1.65; max-width: 100%; display: block;\" />");
  }
}
