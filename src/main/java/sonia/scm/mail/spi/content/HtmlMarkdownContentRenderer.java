/**
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
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
