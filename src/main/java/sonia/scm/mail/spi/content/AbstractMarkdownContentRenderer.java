/*
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
