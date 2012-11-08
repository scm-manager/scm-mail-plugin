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



package sonia.scm.mail.spi;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.collect.Lists;

import org.codemonkey.simplejavamail.Email;
import org.codemonkey.simplejavamail.MailException;

import sonia.scm.mail.MailContext;
import sonia.scm.mail.MailSendBatchException;
import sonia.scm.mail.MailService;
import sonia.scm.mail.config.MailConfiguration;

/**
 *
 * @author Sebastian Sdorra
 */
public abstract class AbstractMailService implements MailService
{

  /**
   * Constructs ...
   *
   *
   * @param context
   */
  public AbstractMailService(MailContext context)
  {
    this.context = context;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param email
   * @param emails
   *
   * @throws MailException
   * @throws MailSendBatchException
   */
  @Override
  public void send(Email email, Email... emails)
    throws MailException, MailSendBatchException
  {
    send(context.getConfiguration(), Lists.asList(email, emails));
  }

  /**
   * Method description
   *
   *
   * @param emails
   *
   * @throws MailException
   * @throws MailSendBatchException
   */
  @Override
  public void send(Iterable<Email> emails)
    throws MailException, MailSendBatchException
  {
    send(context.getConfiguration(), emails);
  }

  /**
   * Method description
   *
   *
   * @param configuration
   * @param email
   * @param emails
   *
   * @throws MailException
   * @throws MailSendBatchException
   */
  @Override
  public void send(MailConfiguration configuration, Email email,
    Email... emails)
    throws MailException, MailSendBatchException
  {
    send(configuration, Lists.asList(email, emails));
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  protected MailContext context;
}
