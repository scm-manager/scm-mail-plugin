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

import java.util.Locale;

interface ContentRenderer {

  MailContent render(Context context);

  class Context {
    private final String templatePath;
    private final Locale preferredLocale;
    private final Object templateModel;

    Context(String templatePath, Locale preferredLocale, Object templateModel) {
      this.templatePath = templatePath;
      this.preferredLocale = preferredLocale;
      this.templateModel = templateModel;
    }

    String getTemplatePath() {
      return templatePath;
    }

    Locale getPreferredLocale() {
      return preferredLocale;
    }

    Object getTemplateModel() {
      return templateModel;
    }
  }

}
