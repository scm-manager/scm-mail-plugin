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
import sonia.scm.mail.api.MailTemplateType;

import java.util.Map;

public class MailContentRendererFactory {

  private final Map<MailTemplateType, ContentRenderer> rendererMap;

  @Inject
  public MailContentRendererFactory(Map<MailTemplateType, ContentRenderer> rendererMap) {
    this.rendererMap = rendererMap;
  }

  public MailContentRenderer createMailContentRenderer(String templatePath, MailTemplateType templateType) {
    ContentRenderer contentRenderer = rendererMap.get(templateType);
    return new MailContentRenderer(contentRenderer, templatePath);
  }
}
