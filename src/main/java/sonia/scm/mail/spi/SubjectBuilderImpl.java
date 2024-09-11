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

package sonia.scm.mail.spi;

import lombok.AccessLevel;
import lombok.Getter;
import sonia.scm.mail.api.MailService;
import sonia.scm.mail.api.MailTemplateType;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

class SubjectBuilderImpl implements MailService.SubjectBuilder {

  private final EnvelopeBuilderImpl envelopeBuilder;

  @Getter(value = AccessLevel.PACKAGE)
  private final String defaultSubject;

  @Getter(value = AccessLevel.PACKAGE)
  private final Map<Locale, String> localized = new HashMap<>();

  SubjectBuilderImpl(EnvelopeBuilderImpl envelopeBuilder, String defaultSubject) {
    this.envelopeBuilder = envelopeBuilder;
    this.defaultSubject = defaultSubject;
  }

  @Override
  public MailService.SubjectBuilder withSubject(Locale locale, String subject) {
    this.localized.put(locale, subject);
    return this;
  }

  @Override
  public MailService.TemplateBuilder withTemplate(String template, MailTemplateType type) {
    return new TemplateBuilderImpl(envelopeBuilder, this, template, type);
  }
}
