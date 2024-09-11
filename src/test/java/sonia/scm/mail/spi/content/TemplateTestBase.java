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
