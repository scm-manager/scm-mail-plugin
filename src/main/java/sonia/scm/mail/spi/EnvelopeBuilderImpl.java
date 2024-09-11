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

import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.Getter;
import org.apache.shiro.SecurityUtils;
import sonia.scm.mail.api.MailConfiguration;
import sonia.scm.mail.api.MailService;
import sonia.scm.mail.api.ScmRecipient;
import sonia.scm.mail.api.Topic;
import sonia.scm.user.User;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

class EnvelopeBuilderImpl implements MailService.EnvelopeBuilder {

  @Getter(value = AccessLevel.PACKAGE)
  private final MailCreationContext mailCreationContext;
  @Getter(value = AccessLevel.PACKAGE)
  private final Set<String> users = new HashSet<>();
  @Getter(value = AccessLevel.PACKAGE)
  private final Set<Recipient> external = new HashSet<>();
  @Getter(value = AccessLevel.PACKAGE)
  private String fromDisplayName;
  @Getter(value = AccessLevel.PACKAGE)
  private String fromAddress;
  @Getter(value = AccessLevel.PACKAGE)
  private Topic topic;
  @Getter(value = AccessLevel.PACKAGE)
  private String entityId;


  EnvelopeBuilderImpl(MailCreationContext mailCreationContext) {
    this.mailCreationContext = mailCreationContext;
  }

  @Override
  public MailService.EnvelopeBuilder withConfiguration(MailConfiguration configuration) {
    this.getMailCreationContext().setConfiguration(configuration);
    return this;
  }

  @Override
  public MailService.EnvelopeBuilder from(String displayName) {
    this.fromDisplayName = displayName;
    return this;
  }

  @Override
  public MailService.EnvelopeBuilder from(ScmRecipient from) {
    if (from == null) {
      return this;
    }

    this.fromDisplayName = from.getDisplayName();
    if (!Strings.isNullOrEmpty(from.getAddress())) {
      this.fromAddress = from.getAddress();
    }

    return this;
  }

  @Override
  public MailService.EnvelopeBuilder fromCurrentUser() {
    User user = SecurityUtils.getSubject().getPrincipals().oneByType(User.class);
    fromDisplayName = user.getDisplayName();
    if (!Strings.isNullOrEmpty(user.getMail())) {
      fromAddress = user.getMail();
    }
    return this;
  }

  @Override
  public MailService.EnvelopeBuilder toUser(String userId) {
    users.add(userId);
    return this;
  }

  @Override
  public MailService.EnvelopeBuilder toAddress(String emailAddress) {
    external.add(new Recipient(emailAddress));
    return this;
  }

  @Override
  public MailService.EnvelopeBuilder toAddress(String displayName, String emailAddress) {
    external.add(new Recipient(displayName, emailAddress));
    return this;
  }

  @Override
  public MailService.EnvelopeBuilder toAddress(Locale locale, String displayName, String emailAddress) {
    external.add(new Recipient(locale, displayName, emailAddress));
    return this;
  }

  @Override
  public MailService.EnvelopeBuilder toAddress(Locale locale, String emailAddress) {
    external.add(new Recipient(locale, emailAddress));
    return this;
  }

  @Override
  public MailService.EnvelopeBuilder onTopic(Topic topic) {
    this.topic = topic;
    return this;
  }

  @Override
  public MailService.EnvelopeBuilder onEntity(String entityId) {
    this.entityId = entityId;
    return this;
  }

  String effectiveFromAddress() {
    return !Strings.isNullOrEmpty(fromAddress) ? fromAddress : mailCreationContext.getConfiguration().getFrom();
  }

  @Override
  public MailService.SubjectBuilder withSubject(String subject) {
    return new SubjectBuilderImpl(this, subject);
  }
}
