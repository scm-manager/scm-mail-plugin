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
