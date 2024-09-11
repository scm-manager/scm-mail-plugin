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

package sonia.scm.mail.spi;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.collect.Lists;
import org.simplejavamail.MailException;
import org.simplejavamail.api.email.Email;
import sonia.scm.mail.api.MailConfiguration;
import sonia.scm.mail.api.MailContext;
import sonia.scm.mail.api.MailSendBatchException;
import sonia.scm.mail.api.MailService;

/**
 * @author Sebastian Sdorra
 */
public abstract class AbstractMailService implements MailService {

  /**
   * Constructs ...
   *
   * @param context
   */
  public AbstractMailService(MailContext context) {
    this.context = context;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   * @param email
   * @param emails
   * @throws MailException
   * @throws MailSendBatchException
   */
  @Override
  public void send(Email email, Email... emails)
    throws MailException, MailSendBatchException {
    send(context.getConfiguration(), Lists.asList(email, emails));
  }

  /**
   * Method description
   *
   * @param emails
   * @throws MailException
   * @throws MailSendBatchException
   */
  @Override
  public void send(Iterable<Email> emails)
    throws MailException, MailSendBatchException {
    send(context.getConfiguration(), emails);
  }

  /**
   * Method description
   *
   * @param configuration
   * @param email
   * @param emails
   * @throws MailException
   * @throws MailSendBatchException
   */
  @Override
  public void send(MailConfiguration configuration, Email email,
                   Email... emails)
    throws MailException, MailSendBatchException {
    send(configuration, Lists.asList(email, emails));
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   * @return
   */
  @Override
  public boolean isConfigured() {
    return context.getConfiguration().isValid();
  }

  protected MailContext getContext() {
    return context;
  }

  //~--- fields ---------------------------------------------------------------

  /**
   * Field description
   */
  private final MailContext context;
}
