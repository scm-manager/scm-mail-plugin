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

import com.google.common.base.Strings;
import com.google.inject.Inject;

import org.codemonkey.simplejavamail.Email;
import org.codemonkey.simplejavamail.MailException;
import org.codemonkey.simplejavamail.Mailer;
import org.codemonkey.simplejavamail.Recipient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.mail.api.MailConfiguration;
import sonia.scm.mail.api.MailContext;
import sonia.scm.mail.api.MailSendBatchException;
import sonia.scm.mail.api.MailSendException;
import sonia.scm.util.AssertUtil;

//~--- JDK imports ------------------------------------------------------------

import java.util.Iterator;

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

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param context
   */
  @Inject
  public DefaultMailService(MailContext context)
  {
    super(context);
  }

  //~--- methods --------------------------------------------------------------

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
    throws MailException, MailSendBatchException
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
  private Mailer createMailer(MailConfiguration configuration)
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

    Recipient from = e.getFromRecipient();

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
  private String getRecipientsString(Iterable<Recipient> recipients)
  {
    StringBuilder content = new StringBuilder();
    Iterator<Recipient> it = recipients.iterator();

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
}
