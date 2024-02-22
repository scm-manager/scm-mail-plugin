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

import com.github.legman.Subscribe;
import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import jakarta.inject.Singleton;
import lombok.AccessLevel;
import lombok.Getter;
import org.simplejavamail.api.email.Email;
import sonia.scm.mail.api.MailConfiguration;
import sonia.scm.mail.api.MailContext;
import sonia.scm.mail.api.MailSendBatchException;
import sonia.scm.mail.api.ScmMail;
import sonia.scm.mail.api.SummarizeMailConfigChangedEvent;
import sonia.scm.mail.spi.content.MailContentRendererFactory;
import sonia.scm.schedule.Scheduler;
import sonia.scm.user.UserDisplayManager;
import sonia.scm.user.UserEvent;

@Singleton
public class DefaultMailService extends AbstractMailService {

  private final UserDisplayManager userDisplayManager;
  private final MailContentRendererFactory mailContentRendererFactory;

  @VisibleForTesting
  @Getter(value = AccessLevel.PACKAGE)
  private final MailSender mailSender;

  @VisibleForTesting
  @Getter(value = AccessLevel.PACKAGE)
  private final MailSummarizer mailSummarizer;

  @Inject
  DefaultMailService(MailContext context,
                     UserDisplayManager userDisplayManager,
                     MailContentRendererFactory mailContentRendererFactory,
                     MailSender mailSender,
                     MailSummaryQueueStore summaryQueueStore,
                     Scheduler scheduler) {
    super(context);
    this.userDisplayManager = userDisplayManager;
    this.mailContentRendererFactory = mailContentRendererFactory;
    this.mailSender = mailSender;
    this.mailSummarizer = new MailSummarizer(summaryQueueStore, this::emailTemplateBuilder, getContext(), scheduler);
  }

  @Override
  public EnvelopeBuilder emailTemplateBuilder() {
    return new EnvelopeBuilderImpl(
      new MailCreationContext(
        getContext().getConfiguration(), mailContentRendererFactory, getContext(), userDisplayManager, this
      )
    );
  }

  @Override
  public void send(MailConfiguration configuration, Iterable<Email> emails) throws MailSendBatchException {
    this.mailSender.send(configuration, emails);
  }

  @Override
  public void addMail(String userId, String category, String entityId, ScmMail mail) throws MailSendBatchException {
    this.mailSummarizer.addMail(userId, category, entityId, mail);
  }

  @Subscribe(async = false)
  public void onSummarizeMailConfigChanged(SummarizeMailConfigChangedEvent event) {
    this.mailSummarizer.onSummarizeMailConfigChanged(event);
  }

  @Subscribe
  public synchronized void onUserDeleted(UserEvent event) {
    this.mailSummarizer.onUserDeleted(event);
  }

}
