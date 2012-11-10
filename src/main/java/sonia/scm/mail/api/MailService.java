/**
 * Copyright (c) 2010, Sebastian Sdorra All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. Neither the name of SCM-Manager;
 * nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.mail.api;

//~--- non-JDK imports --------------------------------------------------------

import org.codemonkey.simplejavamail.Email;
import org.codemonkey.simplejavamail.MailException;

/**
 * Service for sending e-mails.
 *
 * @author Sebastian Sdorra
 */
public interface MailService
{

  /**
   * Send e-mails with the default configuration.
   *
   *
   * @param email e-mail to send
   * @param emails e-mails to send
   *
   * @throws MailException
   * @throws MailSendBatchException
   */
  public void send(Email email, Email... emails)
    throws MailException, MailSendBatchException;

  /**
   * Send e-mails with the default configuration.
   *
   *
   * @param emails e-mails to send
   *
   * @throws MailException
   * @throws MailSendBatchException
   */
  public void send(Iterable<Email> emails)
    throws MailException, MailSendBatchException;

  /**
   * Send e-mails with the given configuration.
   *
   *
   *
   * @param configuration mail configuration
   * @param email e-mail to send
   * @param emails e-mails to send
   *
   * @throws MailException
   * @throws MailSendBatchException
   */
  public void send(MailConfiguration configuration, Email email,
    Email... emails)
    throws MailException, MailSendBatchException;

  /**
   * Send e-mails with the default configuration.
   *
   *
   *
   * @param configuration mail configuration
   * @param emails e-mails to send
   *
   * @throws MailException
   * @throws MailSendBatchException
   */
  public void send(MailConfiguration configuration, Iterable<Email> emails)
    throws MailException, MailSendBatchException;

  //~--- get methods ----------------------------------------------------------

  /**
   * Returns true if default mail configuration is valid.
   *
   *
   * @return true if default mail configuration is valid
   */
  public boolean isConfigured();
}
