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

class TemplateBuilderImpl implements MailService.TemplateBuilder {

  private final EnvelopeBuilderImpl envelopeBuilder;
  private final SubjectBuilderImpl subjectBuilder;

  @Getter(value = AccessLevel.PACKAGE)
  private final String template;

  @Getter(value = AccessLevel.PACKAGE)
  private final MailTemplateType type;

  TemplateBuilderImpl(EnvelopeBuilderImpl envelopeBuilder, SubjectBuilderImpl subjectBuilder, String template, MailTemplateType type) {
    this.envelopeBuilder = envelopeBuilder;
    this.subjectBuilder = subjectBuilder;
    this.template = template;
    this.type = type;
  }

  @Override
  public MailService.MailBuilder andModel(Object templateModel) {
    return new MailBuilderImpl(envelopeBuilder, subjectBuilder, this, templateModel);
  }
}
