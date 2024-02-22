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
