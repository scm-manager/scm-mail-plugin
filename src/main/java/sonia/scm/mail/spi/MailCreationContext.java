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

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import sonia.scm.mail.api.MailConfiguration;
import sonia.scm.mail.api.MailContext;
import sonia.scm.mail.api.MailService;
import sonia.scm.mail.spi.content.MailContentRendererFactory;
import sonia.scm.user.UserDisplayManager;

class MailCreationContext {

  @Getter(value = AccessLevel.PACKAGE)
  @Setter(value = AccessLevel.PACKAGE)
  private MailConfiguration configuration;

  @Getter(value = AccessLevel.PACKAGE)
  private final MailContentRendererFactory mailContentRendererFactory;

  @Getter(value = AccessLevel.PACKAGE)
  private final MailContext mailContext;

  @Getter(value = AccessLevel.PACKAGE)
  private final UserDisplayManager userDisplayManager;

  @Getter(value = AccessLevel.PACKAGE)
  private final MailService mailService;

  MailCreationContext(MailConfiguration configuration,
                      MailContentRendererFactory mailContentRendererFactory,
                      MailContext mailContext,
                      UserDisplayManager userDisplayManager,
                      MailService mailService) {
    this.configuration = configuration;
    this.mailContentRendererFactory = mailContentRendererFactory;
    this.mailContext = mailContext;
    this.userDisplayManager = userDisplayManager;
    this.mailService = mailService;
  }
}
