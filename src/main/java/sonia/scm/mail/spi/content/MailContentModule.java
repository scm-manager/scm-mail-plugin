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

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.MapBinder;
import sonia.scm.mail.api.MailTemplateType;

public class MailContentModule extends AbstractModule {
  @Override
  protected void configure() {
    MapBinder<MailTemplateType, ContentRenderer> contentRendererMapBinder =
      MapBinder.newMapBinder(binder(), MailTemplateType.class, ContentRenderer.class);

    contentRendererMapBinder.addBinding(MailTemplateType.TEXT).to(TextContentRenderer.class);
    contentRendererMapBinder.addBinding(MailTemplateType.HTML).to(HtmlContentRenderer.class);
    contentRendererMapBinder.addBinding(MailTemplateType.MARKDOWN_TEXT).to(TextMarkdownContentRenderer.class);
    contentRendererMapBinder.addBinding(MailTemplateType.MARKDOWN_HTML).to(HtmlMarkdownContentRenderer.class);

    bind(MailContentRendererFactory.class);
  }
}
