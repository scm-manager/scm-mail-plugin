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

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import sonia.scm.template.TemplateEngineFactory;

import java.util.Locale;

abstract class AbstractMarkdownContentRenderer extends AbstractTemplateContentRenderer {

  private static final Parser MARKDOWN_PARSER = Parser.builder().build();

  AbstractMarkdownContentRenderer(TemplateEngineFactory templateEngineFactory) {
    super(templateEngineFactory);
  }

  @Override
  public MailContent render(Context context) {
    String markdown = renderAsString(context);
    Node node = MARKDOWN_PARSER.parse(markdown);
    return render(node, context.getPreferredLocale());
  }

  abstract MailContent render(Node node, Locale locale);
}
