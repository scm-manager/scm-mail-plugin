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

package sonia.scm.mail.api;

//~--- non-JDK imports --------------------------------------------------------

import org.simplejavamail.api.email.Email;

import java.util.Locale;

/**
 * Service for sending e-mails.
 *
 * @author Sebastian Sdorra
 */
public interface MailService {

  /**
   * Send e-mails with the default configuration.
   *
   * @param email  e-mail to send
   * @param emails e-mails to send
   * @throws MailSendBatchException
   */
  void send(Email email, Email... emails) throws MailSendBatchException;

  /**
   * Send e-mails with the default configuration.
   *
   * @param emails e-mails to send
   * @throws MailSendBatchException
   */
  void send(Iterable<Email> emails) throws MailSendBatchException;

  /**
   * Creating and sending email from a template.
   *
   * <pre>
   * service.emailTemplateBuilder()
   *   //.withConfiguration(mailConfiguration) use of non default configuration
   *   .toUser("trillian") // use email and locale from user
   *   .toAddress("arthur.dent@hitchhiker.com") // use default locale
   *   .toAddress(Locale.GERMAN, "tricia.mcmillian@hitchhiker.com")
   *   .withSubject("Default Subject")
   *   .withSubject(Locale.GERMAN, "Auch auf Deutsch")
   *   .withTemplate("/path/to/my/template")
   *   .andModel(new Object())
   *   .send();
   * </pre>
   */
  EnvelopeBuilder emailTemplateBuilder();

  void addMail(String userId, String category, String entityId, ScmMail mail) throws MailSendBatchException;

  /**
   * Send e-mails with the given configuration.
   *
   * @param configuration mail configuration
   * @param email         e-mail to send
   * @param emails        e-mails to send
   * @throws MailSendBatchException
   */
  void send(MailConfiguration configuration, Email email,
            Email... emails)
    throws MailSendBatchException;

  /**
   * Send e-mails with the default configuration.
   *
   * @param configuration mail configuration
   * @param emails        e-mails to send
   * @throws MailSendBatchException
   */
  void send(MailConfiguration configuration, Iterable<Email> emails)
    throws MailSendBatchException;

  //~--- get methods ----------------------------------------------------------

  /**
   * Returns true if default mail configuration is valid.
   *
   * @return true if default mail configuration is valid
   */
  boolean isConfigured();


  /**
   * Builder for the email envelope.
   */
  interface EnvelopeBuilder {

    /**
     * Use alternative mail configuration.
     *
     * @param mailConfiguration alternative mail configuration
     * @return {@code this}
     */
    EnvelopeBuilder withConfiguration(MailConfiguration mailConfiguration);

    /**
     * Use given display name as name for the from address.
     *
     * @param displayName display name
     * @return {@code this}
     */
    EnvelopeBuilder from(String displayName);

    EnvelopeBuilder from(ScmRecipient from);

    /**
     * Use current users display as name for the from address.
     *
     * @return {@code this}
     */
    EnvelopeBuilder fromCurrentUser();

    /**
     * Adds user from the scm-manager user database to the list of recipients. The user is added with his configured
     * email, display name and locale. If the user has no locale configured the default locale is used.
     *
     * @param username user id
     * @return {@code this}
     */
    EnvelopeBuilder toUser(String username);

    /**
     * Adds e-mail address to list of recipients. The address is added with the default locale.
     *
     * @param emailAddress email address
     * @return {@code this}
     */
    EnvelopeBuilder toAddress(String emailAddress);

    /**
     * Adds e-mail address with display name to list of recipients. The address is added with the default locale.
     *
     * @param displayName  display name
     * @param emailAddress email address
     * @return {@code this}
     */
    EnvelopeBuilder toAddress(String displayName, String emailAddress);

    /**
     * Adds e-mail address to list of recipients.
     *
     * @param locale       preferred locale for subject and mail content
     * @param emailAddress email address
     * @return {@code this}
     */
    EnvelopeBuilder toAddress(Locale locale, String emailAddress);

    /**
     * Adds e-mail address to list of recipients.
     *
     * @param locale       preferred locale for subject and mail content
     * @param displayName  display name
     * @param emailAddress email address
     * @return {@code this}
     */
    EnvelopeBuilder toAddress(Locale locale, String displayName, String emailAddress);

    /**
     * If this is set, emails are only sent to those recipients, that have not unsubscribed from the given topic.
     *
     * @param topic The topic for this mail.
     * @return {@code this}
     */
    EnvelopeBuilder onTopic(Topic topic);

    EnvelopeBuilder onEntity(String entityId);

    /**
     * Sets the default subject for the mail and returns the next step of the builder.
     *
     * @param subject default mail subject
     * @return subject step of builder
     */
    SubjectBuilder withSubject(String subject);
  }

  /**
   * Subject step of email builder.
   */
  interface SubjectBuilder {

    /**
     * Sets the subject for the given language.
     *
     * @param locale  locale
     * @param subject subject
     * @return {@code this}
     */
    SubjectBuilder withSubject(Locale locale, String subject);

    /**
     * Sets the resource path for the template and returns the template step of builder. Please have look at
     * {@link sonia.scm.template.TemplateEngine#getTemplate(String, Locale)} for details of template resolution.
     *
     * @param template template path
     * @param type     rendering and output type
     * @return template builder step
     */
    TemplateBuilder withTemplate(String template, MailTemplateType type);

  }

  /**
   * Template step of email builder.
   */
  @FunctionalInterface
  interface TemplateBuilder {

    /**
     * Sets the template model for the rendering process of the mail content and returns send step of the builder.
     *
     * @param templateModel model of template
     * @return send step
     */
    MailBuilder andModel(Object templateModel);

  }

  /**
   * Send step of email builder.
   */
  interface MailBuilder {

    /**
     * Builds and send emails to the configured recipients.
     *
     * @throws MailSendBatchException
     */
    void send() throws MailSendBatchException;

    void queueMails() throws MailSendBatchException;
  }
}
