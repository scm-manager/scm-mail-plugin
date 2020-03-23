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

import sonia.scm.template.Template;
import sonia.scm.template.TemplateEngine;
import sonia.scm.template.TemplateEngineFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Locale;

abstract class AbstractTemplateContentRenderer implements ContentRenderer {

  private final TemplateEngineFactory templateEngineFactory;

  AbstractTemplateContentRenderer(TemplateEngineFactory templateEngineFactory) {
    this.templateEngineFactory = templateEngineFactory;
  }

  String renderAsString(Context context) {
    Template template = readTemplate(context);
    return renderTemplate(template, context.getTemplateModel());
  }

  String renderTemplate(Template template, Object templateModel) {
    try (StringWriter writer = new StringWriter()) {
      template.execute(writer, templateModel);
      return writer.toString();
    } catch (IOException ex) {
      throw new MailContentException("failed to render template: " + template, ex);
    }
  }

  private Template readTemplate(Context context) {
    return readTemplate(context.getTemplatePath(), context.getPreferredLocale());
  }

  Template readTemplate(String templatePath, Locale preferredLocale) {
    TemplateEngine engine = templateEngineFactory.getEngineByExtension(templatePath);
    if (engine == null) {
      throw new MailContentException("could not find template engine for " + templatePath);
    }

    try {
      return engine.getTemplate(templatePath, preferredLocale);
    } catch (IOException ex) {
      throw new MailContentException("failed to read template " + templatePath, ex);
    }
  }
}
