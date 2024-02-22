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

import com.google.common.base.Stopwatch;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import org.simplejavamail.api.email.Email;
import org.simplejavamail.api.email.EmailPopulatingBuilder;
import org.simplejavamail.email.EmailBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.mail.api.MailContext;
import sonia.scm.mail.api.MailSendBatchException;
import sonia.scm.mail.api.MailService;
import sonia.scm.mail.api.MailTemplateType;
import sonia.scm.mail.api.ScmMail;
import sonia.scm.mail.api.ScmRecipient;
import sonia.scm.mail.api.Topic;
import sonia.scm.mail.api.UserMailConfiguration;
import sonia.scm.mail.spi.content.MailContent;
import sonia.scm.mail.spi.content.MailContentRenderer;
import sonia.scm.user.DisplayUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

class MailBuilderImpl implements MailService.MailBuilder {

  private static final Logger LOG = LoggerFactory.getLogger(MailBuilderImpl.class);
  private final EnvelopeBuilderImpl envelopeBuilder;
  private final SubjectBuilderImpl subjectBuilder;
  private final TemplateBuilderImpl templateBuilder;
  private final Object model;

  MailBuilderImpl(EnvelopeBuilderImpl envelopeBuilder, SubjectBuilderImpl subjectBuilder, TemplateBuilderImpl templateBuilder, Object model) {
    this.envelopeBuilder = envelopeBuilder;
    this.subjectBuilder = subjectBuilder;
    this.templateBuilder = templateBuilder;
    this.model = model;
  }

  @Override
  public void send() throws MailSendBatchException {
    List<Email> emails = new ArrayList<>();
    for (Recipient recipient : collectRecipients()) {
      emails.add(createMail(recipient));
    }
    envelopeBuilder.getMailCreationContext().getMailService().send(
      envelopeBuilder.getMailCreationContext().getConfiguration(), emails
    );
  }

  @Override
  public void queueMails() throws MailSendBatchException {
    for (Recipient recipient : collectUserRecipients()) {
      envelopeBuilder.getMailCreationContext().getMailService().addMail(
        recipient.getUserId(),
        envelopeBuilder.getTopic().getCategory().getName(),
        envelopeBuilder.getEntityId(),
        createScmMail(recipient)
      );
    }

  }

  private Email createMail(Recipient recipient) {
    MailContent mailContent = createMailContent(recipient);
    String effectiveFromAddress = envelopeBuilder.effectiveFromAddress();

    EmailPopulatingBuilder emailBuilder = EmailBuilder.startingBlank();

    if (!Strings.isNullOrEmpty(effectiveFromAddress)) {
      emailBuilder.from(envelopeBuilder.getFromDisplayName(), effectiveFromAddress);
    }

    return emailBuilder
      .to(new org.simplejavamail.api.email.Recipient(
        recipient.getDisplayName(), recipient.getAddress(), jakarta.mail.Message.RecipientType.TO)
      )
      .withSubject(subjectFor(recipient))
      .withHTMLText(mailContent.getHtml())
      .withPlainText(mailContent.getText())
      .buildEmail();
  }

  private ScmMail createScmMail(Recipient recipient) {
    String effectiveFromAddress = envelopeBuilder.effectiveFromAddress();

    ScmMail mail = new ScmMail();

    mail.setCategory(envelopeBuilder.getTopic().getCategory().getName());
    mail.setEntityId(envelopeBuilder.getEntityId());
    mail.setFrom(new ScmRecipient(envelopeBuilder.getFromDisplayName(), effectiveFromAddress));
    mail.setTo(new ScmRecipient(recipient.getDisplayName(), recipient.getAddress()));
    mail.setSubject(subjectFor(recipient));
    mail.setPlainText(createPlainTextMailContent(recipient).getText());

    return mail;
  }

  private MailContent createMailContent(Recipient recipient) {
    Stopwatch sw = Stopwatch.createStarted();
    try {
      MailContentRenderer mailContentRenderer = envelopeBuilder
        .getMailCreationContext()
        .getMailContentRendererFactory()
        .createMailContentRenderer(
          templateBuilder.getTemplate(), templateBuilder.getType()
        );

      return mailContentRenderer.createMailContent(recipient.getLocale(), model);
    } finally {
      LOG.trace("mail content rendered in {}", sw.stop());
    }
  }

  private MailContent createPlainTextMailContent(Recipient recipient) {
    Stopwatch sw = Stopwatch.createStarted();
    try {
      MailContentRenderer mailContentRenderer = envelopeBuilder
        .getMailCreationContext()
        .getMailContentRendererFactory()
        .createMailContentRenderer(
          templateBuilder.getTemplate(), MailTemplateType.TEXT
        );

      return mailContentRenderer.createMailContent(recipient.getLocale(), model);
    } finally {
      LOG.trace("mail content rendered in {}", sw.stop());
    }
  }

  private String subjectFor(Recipient recipient) {
    String localized = subjectBuilder.getLocalized().get(recipient.getLocale());
    if (Strings.isNullOrEmpty(localized)) {
      LOG.trace("could not find subject with locale {}", recipient.getLocale());
      return subjectBuilder.getDefaultSubject();
    }
    return localized;
  }

  private Set<Recipient> collectRecipients() {
    ImmutableSet.Builder<Recipient> builder = ImmutableSet.builder();
    builder.addAll(collectUserRecipients());
    builder.addAll(collectExternals());
    return builder.build();
  }

  private Set<Recipient> collectExternals() {
    return envelopeBuilder.getExternal()
      .stream()
      .map(this::ensureLocale)
      .collect(Collectors.toSet());
  }

  private Recipient ensureLocale(Recipient recipient) {
    if (recipient.getLocale() == null) {
      return new Recipient(defaultLocale(), recipient.getDisplayName(), recipient.getAddress());
    }
    return recipient;
  }

  private Set<Recipient> collectUserRecipients() {
    return envelopeBuilder.getUsers().stream()
      .filter(this::subscribedToTopic)
      .map(this::mapUserToRecipient)
      .filter(Objects::nonNull)
      .filter(recipient -> Objects.nonNull(recipient.getAddress()))
      .collect(Collectors.toSet());
  }

  private boolean subscribedToTopic(String userId) {
    if (envelopeBuilder.getTopic() == null) {
      return true;
    }
    return envelopeBuilder.getMailCreationContext().getMailContext().getUserConfiguration(userId)
      .map(this::subscribedToTopic)
      .orElse(true);
  }

  private Boolean subscribedToTopic(UserMailConfiguration userMailConfiguration) {
    Set<Topic> excludedTopics = userMailConfiguration.getExcludedTopics();
    return excludedTopics == null || !excludedTopics.contains(envelopeBuilder.getTopic());
  }

  private Recipient mapUserToRecipient(String userId) {
    Optional<DisplayUser> displayUser = envelopeBuilder.getMailCreationContext().getUserDisplayManager().get(userId);
    if (displayUser.isPresent()) {
      DisplayUser user = displayUser.get();
      Locale locale = preferredLocale(userId);
      return new Recipient(locale, user.getDisplayName(), user.getMail(), userId);
    }

    LOG.warn("could not find user {}", userId);
    return null;
  }

  private Locale preferredLocale(String username) {
    MailContext context = envelopeBuilder.getMailCreationContext().getMailContext();
    Locale fallbackLocale = defaultLocale();
    return context.getUserConfiguration(username)
      .map(UserMailConfiguration::getLanguage)
      .map(Locale::new)
      .orElse(fallbackLocale);
  }

  private Locale defaultLocale() {
    return Optional.ofNullable(envelopeBuilder.getMailCreationContext().getConfiguration().getLanguage())
      .map(Locale::new)
      .orElse(Locale.ENGLISH);
  }
}
