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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import org.simplejavamail.MailException;
import org.simplejavamail.api.email.Email;
import org.simplejavamail.api.email.Recipient;
import org.simplejavamail.api.mailer.Mailer;
import org.simplejavamail.email.EmailBuilder;
import org.simplejavamail.mailer.MailerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.mail.api.MailConfiguration;
import sonia.scm.mail.api.MailSendBatchException;
import sonia.scm.mail.api.MailSendException;
import sonia.scm.trace.Span;
import sonia.scm.trace.Tracer;
import sonia.scm.util.AssertUtil;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import java.util.Iterator;
import java.util.Properties;

class MailSender {

  private static final Logger LOG = LoggerFactory.getLogger(MailSender.class);
  private final Tracer tracer;
  private final Provider<SSLContext> sslContext;

  @Inject
  MailSender(Tracer tracer, Provider<SSLContext> sslContext) {
    this.tracer = tracer;
    this.sslContext = sslContext;
  }

  public void send(MailConfiguration configuration, Iterable<Email> emails)
    throws MailSendBatchException {
    if (configuration.isValid()) {
      MailSendBatchException batchEx = null;
      Mailer mailer = createMailer(configuration);

      for (Email e : emails) {
        try (Span span = tracer.span("Mail")) {
          try {
            span.label("url", configuration.getHost() + ":" + configuration.getPort());
            span.label("method", "SMTP");
            sendMail(configuration, mailer, e);
          } catch (MailException ex) {
            span.label("exception", ex.getClass().getName());
            span.label("message", ex.getMessage());
            span.failed();
            LOG.warn("could not send mail", ex);

            if (batchEx == null) {
              batchEx =
                new MailSendBatchException("some messages could not be send");
            }

            batchEx.append(new MailSendException("message could not be send", e,
              ex));
          }
        }
      }

      if (batchEx != null) {
        throw batchEx;
      }

    } else if (LOG.isWarnEnabled()) {
      LOG.warn("mail configuration is not valid");
    }
  }

  @VisibleForTesting
  Mailer createMailer(MailConfiguration configuration) {
    SSLSocketFactory socketFactory = sslContext.get().getSocketFactory();
    Properties props = new Properties();
    props.put("mail.smtp.ssl.socketFactory", socketFactory);
    props.put("mail.smtps.ssl.socketFactory", socketFactory);

    return MailerBuilder
      .withSMTPServer(
        configuration.getHost(),
        configuration.getPort(),
        Strings.emptyToNull(configuration.getUsername()),
        Strings.emptyToNull(configuration.getPassword()))
      .withTransportStrategy(configuration.getTransportStrategy().getTransportStrategy())
      .withProperties(props)
      .buildMailer();
  }

  private void sendMail(MailConfiguration configuration, Mailer mailer, Email email) {
    AssertUtil.assertIsValid(configuration);

    Email emailWithSubjectPrefix = addPrefixToSubject(email, configuration.getSubjectPrefix());
    Email emailWithValidFrom = checkFrom(emailWithSubjectPrefix, configuration.getFrom(), configuration.getFromAddressAsSender());

    if (mailer.validate(emailWithValidFrom)) {
      if (LOG.isDebugEnabled()) {
        LOG.trace("send email to {} from {}",
          getRecipientsString(emailWithValidFrom.getRecipients()),
          emailWithValidFrom.getFromRecipient().getAddress());
      }

      mailer.sendMail(emailWithValidFrom);
    }
  }

  private Email addPrefixToSubject(Email email, String prefix) {
    if (Strings.isNullOrEmpty(prefix)) {
      LOG.trace("no prefix defined");
      return email;
    }

    if (email.getSubject() == null || email.getSubject().startsWith(prefix)) {
      return email;
    }

    String paddedPrefix = prefix;
    if (!paddedPrefix.endsWith(" ")) {
      paddedPrefix = paddedPrefix.concat(" ");
    }

    return EmailBuilder.copying(email).withSubject(paddedPrefix.concat(email.getSubject())).buildEmail();
  }

  private Email checkFrom(Email email, String fromAddress, boolean fromAddressAsSender) {

    if (email.getFromRecipient() != null && !fromAddressAsSender) {
      LOG.trace("use recipient for {} sending", email.getFromRecipient().getAddress());
      return email;
    }

    if (Strings.isNullOrEmpty(fromAddress)) {
      LOG.trace("no from recipient found and default one is not set");
      return email;
    }

    LOG.trace("no from recipient found setting default one: {}", fromAddress);
    return EmailBuilder.copying(email).from(fromAddress).buildEmail();
  }

  private String getRecipientsString(Iterable<org.simplejavamail.api.email.Recipient> recipients) {
    StringBuilder content = new StringBuilder();
    Iterator<Recipient> it = recipients.iterator();

    while (it.hasNext()) {
      content.append(it.next().getAddress());

      if (it.hasNext()) {
        content.append(", ");
      }
    }

    return content.toString();
  }
}
