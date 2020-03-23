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
package sonia.scm.mail.api;

//~--- non-JDK imports --------------------------------------------------------

import org.codemonkey.simplejavamail.Email;

/**
 * Exception is thrown when a mail sending error is encountered.
 *
 * @author Sebastian Sdorra
 */
public class MailSendException extends Exception
{

  /** serial version uid */
  private static final long serialVersionUID = -5365072368208361896L;

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs a new MailSendException.
   *
   *
   * @param message message message for the exception
   * @param email e-mail failed to send
   * @param cause cause of the exception
   */
  public MailSendException(String message, Email email, Throwable cause)
  {
    super(message, cause);
    this.email = email;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Returns e-mail failed to send
   *
   *
   * @return e-mail failed to send
   */
  public Email getEmail()
  {
    return email;
  }

  //~--- fields ---------------------------------------------------------------

  /** e-mail failed to send */
  private Email email;
}
