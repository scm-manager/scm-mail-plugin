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
