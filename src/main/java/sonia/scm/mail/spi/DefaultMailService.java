/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.mail.spi;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import org.apache.shiro.SecurityUtils;
import org.codemonkey.simplejavamail.Email;
import org.codemonkey.simplejavamail.MailException;
import org.codemonkey.simplejavamail.Mailer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.mail.api.MailConfiguration;
import sonia.scm.mail.api.MailContext;
import sonia.scm.mail.api.MailSendBatchException;
import sonia.scm.mail.api.MailSendException;
import sonia.scm.mail.api.UserMailConfiguration;
import sonia.scm.user.DisplayUser;
import sonia.scm.user.User;
import sonia.scm.user.UserDisplayManager;
import sonia.scm.util.AssertUtil;

import javax.mail.Message;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

//~--- JDK imports ------------------------------------------------------------

/**
 *
 * @author Sebastian Sdorra
 */

public class DefaultMailService extends AbstractMailService
{

  /**
   * the logger for DefaultMailService
   */
  private static final Logger logger =
    LoggerFactory.getLogger(DefaultMailService.class);
  private final UserDisplayManager userDisplayManager;
  private final MailContentRendererFactory mailContentRendererFactory;

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param context
   */
  @Inject
  public DefaultMailService(MailContext context, UserDisplayManager userDisplayManager, MailContentRendererFactory mailContentRendererFactory)
  {
    super(context);
    this.userDisplayManager = userDisplayManager;
    this.mailContentRendererFactory = mailContentRendererFactory;
  }

  //~--- methods --------------------------------------------------------------


  @Override
  public EnvelopeBuilder emailTemplateBuilder() {
    return new EnvelopeBuilderImpl(getContext().getConfiguration());
  }

  /**
   * Method description
   *
   *
   *
   * @param configuration
   * @param emails
   *
   * @throws MailException
   * @throws MailSendBatchException
   */
  @Override
  public void send(MailConfiguration configuration, Iterable<Email> emails)
    throws MailSendBatchException
  {
    if (configuration.isValid())
    {
      MailSendBatchException batchEx = null;
      Mailer mailer = createMailer(configuration);

      for (Email e : emails)
      {
        try
        {
          sendMail(configuration, mailer, e);
        }
        catch (MailException ex)
        {
          logger.warn("could not send mail", ex);

          if (batchEx == null)
          {
            batchEx =
              new MailSendBatchException("some messages could not be send");
          }

          batchEx.append(new MailSendException("message could not be send", e,
            ex));
        }
      }

      if (batchEx != null)
      {
        throw batchEx;
      }

    }
    else if (logger.isWarnEnabled())
    {
      logger.warn("mail configuration is not valid");
    }
  }

  /**
   * Method description
   *
   *
   * @param configuration
   *
   * @return
   */
  @VisibleForTesting
  Mailer createMailer(MailConfiguration configuration)
  {
    //J-
    return new Mailer(
      configuration.getHost(), 
      configuration.getPort(),
      Strings.emptyToNull(configuration.getUsername()),
      Strings.emptyToNull(configuration.getPassword()),
      configuration.getTransportStrategy()
    );
    //J+
  }

  /**
   * Method description
   *
   *
   * @param configuration
   * @param mailer
   * @param e
   */
  private void sendMail(MailConfiguration configuration, Mailer mailer, Email e)
  {
    AssertUtil.assertIsValid(configuration);

    String prefix = configuration.getSubjectPrefix();

    if (!Strings.isNullOrEmpty(prefix))
    {
      String subject = e.getSubject();

      if ((subject != null) &&!subject.startsWith(prefix))
      {
        String ns = prefix;

        if (!ns.endsWith(" "))
        {
          ns = ns.concat(" ");
        }

        e.setSubject(ns.concat(subject));
      }

    }
    else if (logger.isTraceEnabled())
    {
      logger.trace("no prefix defined");
    }

    org.codemonkey.simplejavamail.Recipient from = e.getFromRecipient();

    if (from == null)
    {
      logger.trace("no from recipient found setting default one: {}",
        configuration.getFrom());
      e.setFromAddress(null, configuration.getFrom());
    }
    else if (logger.isTraceEnabled())
    {
      logger.trace("use recipient for {} sending", from.getAddress());
    }

    if (mailer.validate(e))
    {
      if (logger.isDebugEnabled())
      {
        logger.debug("send email to {} from {}",
          getRecipientsString(e.getRecipients()),
          e.getFromRecipient().getAddress());
      }

      mailer.sendMail(e);
    }
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param recipients
   *
   * @return
   */
  private String getRecipientsString(Iterable<org.codemonkey.simplejavamail.Recipient> recipients)
  {
    StringBuilder content = new StringBuilder();
    Iterator<org.codemonkey.simplejavamail.Recipient> it = recipients.iterator();

    while (it.hasNext())
    {
      content.append(it.next().getAddress());

      if (it.hasNext())
      {
        content.append(", ");
      }
    }

    return content.toString();
  }

  private class Recipient {

    private Locale locale;
    private String displayName;
    private String address;

    private Recipient(String address) {
      this.address = address;
    }

    private Recipient(String displayName, String address) {
      this.displayName = displayName;
      this.address = address;
    }

    private Recipient(Locale locale, String address) {
      this.locale = locale;
      this.address = address;
    }

    private Recipient(Locale locale, String displayName, String address) {
      this.locale = locale;
      this.displayName = displayName;
      this.address = address;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof Recipient)) return false;
      Recipient that = (Recipient) o;
      return address.equals(that.address);
    }

    @Override
    public int hashCode() {
      return Objects.hash(locale, address);
    }
  }

  public class EnvelopeBuilderImpl implements EnvelopeBuilder {

    private MailConfiguration configuration;
    private String fromDisplayName;
    private final Set<String> users = new HashSet<>();
    private final Set<Recipient> external = new HashSet<>();

    private EnvelopeBuilderImpl(MailConfiguration configuration) {
      this.configuration = configuration;
    }

    @Override
    public EnvelopeBuilder withConfiguration(MailConfiguration configuration) {
      this.configuration = configuration;
      return this;
    }

    @Override
    public EnvelopeBuilder from(String displayName) {
      this.fromDisplayName = displayName;
      return this;
    }

    @Override
    public EnvelopeBuilder fromCurrentUser() {
      User user = SecurityUtils.getSubject().getPrincipals().oneByType(User.class);
      fromDisplayName = user.getDisplayName();
      return this;
    }

    @Override
    public EnvelopeBuilder toUser(String userId) {
      users.add(userId);
      return this;
    }

    @Override
    public EnvelopeBuilder toAddress(String emailAddress) {
      external.add(new Recipient(emailAddress));
      return this;
    }

    @Override
    public EnvelopeBuilder toAddress(String displayName, String emailAddress) {
      external.add(new Recipient(displayName, emailAddress));
      return this;
    }

    @Override
    public EnvelopeBuilder toAddress(Locale locale, String displayName, String emailAddress) {
      external.add(new Recipient(locale, displayName, emailAddress));
      return this;
    }

    @Override
    public EnvelopeBuilder toAddress(Locale locale, String emailAddress) {
      external.add(new Recipient(locale, emailAddress));
      return this;
    }

    @Override
    public SubjectBuilderImpl withSubject(String subject) {
      return new SubjectBuilderImpl(this, subject);
    }
  }

  public class SubjectBuilderImpl implements SubjectBuilder {

    private EnvelopeBuilderImpl envelopeBuilder;

    private final String defaultSubject;
    private final Map<Locale, String> localized = new HashMap<>();

    private SubjectBuilderImpl(EnvelopeBuilderImpl envelopeBuilder, String defaultSubject) {
      this.envelopeBuilder = envelopeBuilder;
      this.defaultSubject = defaultSubject;
    }

    @Override
    public SubjectBuilder withSubject(Locale locale, String subject) {
      this.localized.put(locale, subject);
      return this;
    }

    @Override
    public TemplateBuilder withTemplate(String template) {
      return new TemplateBuilderImpl(envelopeBuilder, this, template);
    }
  }

  public class TemplateBuilderImpl implements TemplateBuilder {

    private final EnvelopeBuilderImpl envelopeBuilder;
    private final SubjectBuilderImpl subjectBuilder;
    private final String template;

    private TemplateBuilderImpl(EnvelopeBuilderImpl envelopeBuilder, SubjectBuilderImpl subjectBuilder, String template) {
      this.envelopeBuilder = envelopeBuilder;
      this.subjectBuilder = subjectBuilder;
      this.template = template;
    }

    @Override
    public MailBuilder andModel(Object templateModel) {
      return new MailBuilderImpl(envelopeBuilder, subjectBuilder, this, templateModel);
    }
  }

  public class MailBuilderImpl implements MailBuilder {

    private final EnvelopeBuilderImpl envelopeBuilder;
    private final SubjectBuilderImpl subjectBuilder;
    private final TemplateBuilderImpl templateBuilder;
    private final Object model;

    private MailBuilderImpl(EnvelopeBuilderImpl envelopeBuilder, SubjectBuilderImpl subjectBuilder, TemplateBuilderImpl templateBuilder, Object model) {
      this.envelopeBuilder = envelopeBuilder;
      this.subjectBuilder = subjectBuilder;
      this.templateBuilder = templateBuilder;
      this.model = model;
    }

    @Override
    public void send() throws IOException, MailSendBatchException {
      List<Email> emails = new ArrayList<>();
      for (Recipient recipient : collectRecipients()) {
        emails.add(createMail(recipient));
      }
      DefaultMailService.this.send(envelopeBuilder.configuration, emails);
    }

    private Email createMail(Recipient recipient) throws IOException {
      Email email = new Email();
      email.setFromAddress(envelopeBuilder.fromDisplayName, envelopeBuilder.configuration.getFrom());
      email.addRecipient(recipient.displayName, recipient.address, Message.RecipientType.TO);
      email.setSubject(subjectFor(recipient));
      email.setTextHTML(createMailContent(recipient));
      return email;
    }

    private String createMailContent(Recipient recipient) throws IOException {
      MailContentRenderer mailContentRenderer = mailContentRendererFactory.createMailContentRenderer(templateBuilder.template);
      return mailContentRenderer.createMailContent(recipient.locale, model);
    }

    private String subjectFor(Recipient recipient) {
      String localized = subjectBuilder.localized.get(recipient.locale);
      if (Strings.isNullOrEmpty(localized)) {
        logger.debug("could not find subject with locale {}", recipient.locale);
        return subjectBuilder.defaultSubject;
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
      return envelopeBuilder.external
        .stream()
        .map(this::ensureLocale)
        .collect(Collectors.toSet());
    }

    private Recipient ensureLocale(Recipient recipient) {
      if (recipient.locale == null) {
        return new Recipient(defaultLocale(), recipient.displayName, recipient.address);
      }
      return recipient;
    }

    private Set<Recipient> collectUserRecipients() {
      return envelopeBuilder.users.stream()
        .map(this::mapUserToRecipient)
        .filter(Objects::nonNull)
        .collect(Collectors.toSet());
    }

    private Recipient mapUserToRecipient(String username) {
      Optional<DisplayUser> displayUser = userDisplayManager.get(username);
      if (displayUser.isPresent()) {
        DisplayUser user = displayUser.get();
        Locale locale = preferredLocale(username);
        return new Recipient(locale, user.getDisplayName(), user.getMail());
      }

      logger.warn("could not find user {}", username);
      return null;
    }

    private Locale preferredLocale(String username) {
      MailContext context = getContext();
      Locale fallbackLocale = defaultLocale();
      return context.getUserConfiguration(username)
        .map(UserMailConfiguration::getLanguage)
        .map(Locale::new)
        .orElse(fallbackLocale);
    }

    private Locale defaultLocale() {
      return Optional.ofNullable(envelopeBuilder.configuration.getLanguage())
          .map(Locale::new)
          .orElse(Locale.ENGLISH);
    }
  }
}
