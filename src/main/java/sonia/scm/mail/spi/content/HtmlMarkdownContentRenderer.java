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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import jakarta.inject.Inject;
import org.commonmark.node.Image;
import org.commonmark.node.Node;
import org.commonmark.node.Text;
import org.commonmark.renderer.NodeRenderer;
import org.commonmark.renderer.Renderer;
import org.commonmark.renderer.html.CoreHtmlNodeRenderer;
import org.commonmark.renderer.html.HtmlNodeRendererContext;
import org.commonmark.renderer.html.HtmlRenderer;
import org.commonmark.renderer.text.CoreTextContentNodeRenderer;
import org.commonmark.renderer.text.TextContentNodeRendererContext;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.template.Template;
import sonia.scm.template.TemplateEngineFactory;

import java.util.Locale;
import java.util.Set;

class HtmlMarkdownContentRenderer extends AbstractMarkdownContentRenderer {

  @VisibleForTesting
  static final String TEMPLATE_PATH = "/sonia/scm/mail/layout.mustache";

  private static final Renderer HTML_RENDERER = HtmlRenderer.builder()
    .attributeProviderFactory(c -> new HtmlMailAttributeProvider())
    .nodeRendererFactory(ImageNodeRenderer::new)
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
  protected MailContent render(Node node, Locale locale) {
    String text = textRenderer.renderAsString(node);
    String html = renderHtml(node, locale);
    return MailContent.textAndHtml(text, html);
  }

  private String renderHtml(Node node, Locale locale) {
    String html = HTML_RENDERER.render(node);
    return renderTemplate(layoutTemplate, new LayoutModel(html, configuration.getBaseUrl(), locale.getLanguage()));
  }

  private static final class LayoutModel {

    private final String content;
    private final String url;
    private final String language;

    private LayoutModel(String content, String url, String language) {
      this.content = content;
      this.url = url;
      this.language = language;
    }

    public String getContent() {
      return content;
    }

    public String getUrl() {
      return url;
    }

    public String getLanguage() {
      return language;
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this)
        .add("content", content)
        .add("url", url)
        .add("language", language)
        .toString();
    }
  }

  private static class ImageNodeRenderer implements NodeRenderer {

    private final CoreHtmlNodeRenderer renderer;

    ImageNodeRenderer(HtmlNodeRendererContext context) {
      renderer = new CoreHtmlNodeRenderer(context);
    }

    @Override
    public Set<Class<? extends Node>> getNodeTypes() {
      return Set.of(Image.class);
    }

    @Override
    public void render(Node node) {
      Image imageNode = (Image) node;

      if (!imageNode.getDestination().startsWith("http")) {
        imageNode.setDestination("");
      }

      if (imageNode.getFirstChild() == null) {
        imageNode.appendChild(new Text("[Image]"));
      }

      renderer.render(imageNode);
    }
  }
}
