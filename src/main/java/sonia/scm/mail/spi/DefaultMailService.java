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

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Stopwatch;
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
import sonia.scm.mail.api.MailTemplateType;
import sonia.scm.mail.api.Topic;
import sonia.scm.mail.api.UserMailConfiguration;
import sonia.scm.mail.spi.content.MailContent;
import sonia.scm.mail.spi.content.MailContentRenderer;
import sonia.scm.mail.spi.content.MailContentRendererFactory;
import sonia.scm.user.DisplayUser;
import sonia.scm.user.User;
import sonia.scm.user.UserDisplayManager;
import sonia.scm.util.AssertUtil;

import javax.mail.Message;
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
    private Topic topic;
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
    public EnvelopeBuilder onTopic(Topic topic) {
      this.topic = topic;
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
    public TemplateBuilder withTemplate(String template, MailTemplateType type) {
      return new TemplateBuilderImpl(envelopeBuilder, this, template, type);
    }
  }

  public class TemplateBuilderImpl implements TemplateBuilder {

    private final EnvelopeBuilderImpl envelopeBuilder;
    private final SubjectBuilderImpl subjectBuilder;
    private final String template;
    private final MailTemplateType type;

    private TemplateBuilderImpl(EnvelopeBuilderImpl envelopeBuilder, SubjectBuilderImpl subjectBuilder, String template, MailTemplateType type) {
      this.envelopeBuilder = envelopeBuilder;
      this.subjectBuilder = subjectBuilder;
      this.template = template;
      this.type = type;
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
    public void send() throws MailSendBatchException {
      List<Email> emails = new ArrayList<>();
      for (Recipient recipient : collectRecipients()) {
        emails.add(createMail(recipient));
      }
      DefaultMailService.this.send(envelopeBuilder.configuration, emails);
    }

    private Email createMail(Recipient recipient) {
      Email email = new Email();
      email.setFromAddress(envelopeBuilder.fromDisplayName, envelopeBuilder.configuration.getFrom());
      email.addRecipient(recipient.displayName, recipient.address, Message.RecipientType.TO);
      email.setSubject(subjectFor(recipient));

      MailContent mailContent = createMailContent(recipient);
      email.setTextHTML(mailContent.getHtml());
      email.setText(mailContent.getText());
      return email;
    }

    private MailContent createMailContent(Recipient recipient) {
      Stopwatch sw = Stopwatch.createStarted();
      try {
        MailContentRenderer mailContentRenderer = mailContentRendererFactory.createMailContentRenderer(templateBuilder.template, templateBuilder.type);
        return mailContentRenderer.createMailContent(recipient.locale, model);
      } finally {
        logger.debug("mail content rendered in {}", sw.stop());
      }
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
        .filter(this::subscribedToTopic)
        .map(this::mapUserToRecipient)
        .filter(Objects::nonNull)
        .collect(Collectors.toSet());
    }

    private boolean subscribedToTopic(String userId) {
      if (envelopeBuilder.topic == null) {
        return true;
      }
      return getContext().getUserConfiguration(userId)
        .map(this::subscribedToTopic)
        .orElse(true);
    }

    private Boolean subscribedToTopic(UserMailConfiguration userMailConfiguration) {
      Set<Topic> excludedTopics = userMailConfiguration.getExcludedTopics();
      return excludedTopics == null || !excludedTopics.contains(envelopeBuilder.topic);
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
