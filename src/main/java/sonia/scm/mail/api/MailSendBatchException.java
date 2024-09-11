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

import com.google.common.collect.Lists;

import java.util.List;

/**
 * Exception is thrown when one or more mail sending error is encountered.
 *
 * @author Sebastian Sdorra
 */
public class MailSendBatchException extends Exception {

  /**
   * Field description
   */
  private static final long serialVersionUID = 797737794920812556L;

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs a new MailSendBatchException.
   *
   * @param message message for the exception
   */
  public MailSendBatchException(String message) {
    super(message);
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Append a send exception.
   *
   * @param ex send exception
   */
  public void append(MailSendException ex) {
    getSendExceptions().add(ex);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Returns send exceptions.
   *
   * @return send exceptions
   */
  public List<MailSendException> getSendExceptions() {
    if (exceptions == null) {
      exceptions = Lists.newArrayList();
    }

    return exceptions;
  }

  //~--- fields ---------------------------------------------------------------

  /**
   * send exceptions
   */
  private List<MailSendException> exceptions;
}
