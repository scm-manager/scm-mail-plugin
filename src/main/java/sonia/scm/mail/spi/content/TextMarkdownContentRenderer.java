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

import jakarta.inject.Inject;
import org.commonmark.node.Image;
import org.commonmark.node.Node;
import org.commonmark.node.Text;
import org.commonmark.renderer.NodeRenderer;
import org.commonmark.renderer.Renderer;
import org.commonmark.renderer.text.CoreTextContentNodeRenderer;
import org.commonmark.renderer.text.TextContentNodeRendererContext;
import org.commonmark.renderer.text.TextContentRenderer;
import sonia.scm.template.TemplateEngineFactory;

import java.util.Locale;
import java.util.Set;

class TextMarkdownContentRenderer extends AbstractMarkdownContentRenderer {

  private static final Renderer TEXT_RENDERER = TextContentRenderer.builder()
    .nodeRendererFactory(ImageNodeRenderer::new)
    .build();

  @Inject
  TextMarkdownContentRenderer(TemplateEngineFactory templateEngineFactory) {
    super(templateEngineFactory);
  }

  String renderAsString(Node node) {
    return TEXT_RENDERER.render(node);
  }

  @Override
  protected MailContent render(Node node, Locale locale) {
    return MailContent.text(renderAsString(node));
  }

  private static class ImageNodeRenderer implements NodeRenderer {

    private final CoreTextContentNodeRenderer renderer;

    ImageNodeRenderer(TextContentNodeRendererContext context) {
      renderer = new CoreTextContentNodeRenderer(context);
    }

    @Override
    public Set<Class<? extends Node>> getNodeTypes() {
      return Set.of(Image.class);
    }

    @Override
    public void render(Node node) {
      Image imageNode = (Image) node;

      if (imageNode.getFirstChild() == null) {
        imageNode.appendChild(new Text("[Image]"));
      }

      renderer.render(imageNode);
    }
  }
}
