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

/**
 * Exception is thrown when a mail sending error is encountered.
 *
 * @author Sebastian Sdorra
 */
public class MailSendException extends Exception {

  /**
   * serial version uid
   */
  private static final long serialVersionUID = -5365072368208361896L;

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs a new MailSendException.
   *
   * @param message message message for the exception
   * @param email   e-mail failed to send
   * @param cause   cause of the exception
   */
  public MailSendException(String message, Email email, Throwable cause) {
    super(message, cause);
    this.email = email;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Returns e-mail failed to send
   *
   * @return e-mail failed to send
   */
  public Email getEmail() {
    return email;
  }

  //~--- fields ---------------------------------------------------------------

  /**
   * e-mail failed to send
   */
  private Email email;
}
