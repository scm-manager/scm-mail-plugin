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
