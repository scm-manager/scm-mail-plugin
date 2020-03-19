/**
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

import com.google.common.collect.Lists;

import org.codemonkey.simplejavamail.Email;
import org.codemonkey.simplejavamail.MailException;

import sonia.scm.mail.api.MailConfiguration;
import sonia.scm.mail.api.MailContext;
import sonia.scm.mail.api.MailSendBatchException;
import sonia.scm.mail.api.MailService;

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

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public boolean isConfigured()
  {
    return context.getConfiguration().isValid();
  }

  protected MailContext getContext() {
    return context;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private final MailContext context;
}
