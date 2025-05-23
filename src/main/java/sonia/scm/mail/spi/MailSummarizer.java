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
import com.google.common.collect.Maps;
import lombok.AccessLevel;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.HandlerEventType;
import sonia.scm.mail.api.MailContext;
import sonia.scm.mail.api.MailSendBatchException;
import sonia.scm.mail.api.MailService;
import sonia.scm.mail.api.MailTemplateType;
import sonia.scm.mail.api.ScmMail;
import sonia.scm.mail.api.SummarizeMailConfigChangedEvent;
import sonia.scm.mail.api.UserMailConfiguration;
import sonia.scm.schedule.Scheduler;
import sonia.scm.schedule.Task;
import sonia.scm.user.UserEvent;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.Supplier;

import static java.util.Locale.ENGLISH;
import static java.util.Locale.GERMAN;
import static java.util.ResourceBundle.getBundle;

class MailSummarizer {

  private static final String SUMMARY_MAIL_WITH_ENTITY = "summaryMailWithEntity";
  private static final String SUMMARY_MAIL_WITHOUT_ENTITY = "summaryMailWithoutEntity";

  private static final Map<Locale, ResourceBundle> SUBJECT_BUNDLES = Maps
    .asMap(new HashSet<>(Arrays.asList(ENGLISH, GERMAN)), locale -> getBundle("sonia.scm.mail.emailnotification.Subjects", locale));

  private static final String MAIL_SUMMARY_TEMPLATE_PATH = "sonia/scm/mail/emailnotification/mail_summary.mustache";
  private static final String SINGLE_QUEUED_TEMPLATE_PATH = "sonia/scm/mail/emailnotification/single_queued_mail.mustache";
  private static final Logger LOG = LoggerFactory.getLogger(MailSummarizer.class);

  @Getter(value = AccessLevel.PACKAGE)
  @VisibleForTesting
  private final Map<String, MailSummaryQueue> summaryQueuesByUserId;

  private final Map<String, Task> sendMailTasksByUserId = new HashMap<>();

  private final MailSummaryQueueStore summaryQueueStore;
  private final MailContext mailContext;
  private final Scheduler scheduler;
  private final Supplier<MailService.EnvelopeBuilder> envelopeBuilderSupplier;

  MailSummarizer(MailSummaryQueueStore summaryQueueStore,
                 Supplier<MailService.EnvelopeBuilder> envelopeBuilderSupplier,
                 MailContext mailContext,
                 Scheduler scheduler) {
    this.summaryQueueStore = summaryQueueStore;
    this.mailContext = mailContext;
    this.scheduler = scheduler;
    this.envelopeBuilderSupplier = envelopeBuilderSupplier;

    this.summaryQueuesByUserId = this.summaryQueueStore.getAll();
    initSendMailTasks();
  }

  private void initSendMailTasks() {
    this.summaryQueuesByUserId.keySet().forEach(userId -> {
      UserMailConfiguration userConfig = mailContext.getUserConfiguration(userId).orElse(new UserMailConfiguration());

      if (!userConfig.isSummarizeMails()) {
        return;
      }

      scheduleSendMailTask(userId, userConfig.getSummaryFrequency().getCronExpression());
    });
  }

  private void scheduleSendMailTask(String userId, String cronExpression) {
    Task currentTask = this.sendMailTasksByUserId.get(userId);
    if (currentTask != null) {
      LOG.trace("Canceling current send mail task of user {}", userId);
      currentTask.cancel();
    }

    LOG.trace("Schedule sending mails for user {} with frequency {}", userId, cronExpression);
    this.sendMailTasksByUserId.put(
      userId,
      scheduler.schedule(
        cronExpression,
        () -> this.sendQueuedMails(userId)
      )
    );
  }

  public synchronized void addMail(String userId, String category, String entityId, ScmMail mail) throws MailSendBatchException {
    UserMailConfiguration userConfig = mailContext.getUserConfiguration(userId).orElse(new UserMailConfiguration());

    if (!userConfig.isSummarizeMails()) {
      sendSingleMail(userId, mail);
      LOG.trace(
        "Send E-Mail to user {} for category {} and entity {} with subject {}",
        userId,
        category,
        entityId,
        mail.getSubject()
      );
      return;
    }

    MailSummaryQueue userQueue = summaryQueuesByUserId.getOrDefault(userId, new MailSummaryQueue());
    if (!summaryQueuesByUserId.containsKey(userId)) {
      LOG.trace("Create mail queue for user {}", userId);
      summaryQueuesByUserId.put(userId, userQueue);
    }

    if (!sendMailTasksByUserId.containsKey(userId)) {
      this.scheduleSendMailTask(userId, userConfig.getSummaryFrequency().getCronExpression());
    }

    if (userConfig.isSummarizeByEntity()) {
      LOG.trace(
        "Queue E-Mail to user {} for category {} and entity {} with subject {}",
        userId,
        category,
        entityId,
        mail.getSubject()
      );
      userQueue.add(category, entityId, mail);
      this.summaryQueueStore.addUserMailsByCategoryAndEntity(userId, category, entityId, mail);
    } else {
      LOG.trace(
        "Queue E-Mail to user {} for category {} with subject {}",
        userId,
        category,
        mail.getSubject()
      );
      userQueue.add(category, mail);
      this.summaryQueueStore.addUserMailsByCategory(userId, category, mail);
    }
  }

  private void sendSingleMail(String userId, ScmMail mail) throws MailSendBatchException {
    Map<String, String> templateModel = Map.of(
      "content",
      mail.getPlainText()
    );

    MailService.EnvelopeBuilder envelopeBuilder = envelopeBuilderSupplier.get();

    envelopeBuilder
      .toUser(userId)
      .from(mail.getFrom())
      .withSubject(mail.getSubject())
      .withTemplate(SINGLE_QUEUED_TEMPLATE_PATH, MailTemplateType.MARKDOWN_HTML)
      .andModel(templateModel)
      .send();

    LOG.trace("Send single mail for user {}", userId);
  }

  private synchronized void sendQueuedMails(String userId) {
    if (!summaryQueuesByUserId.containsKey(userId)) {
      return;
    }

    MailSummaryQueue summaryQueue = summaryQueuesByUserId.get(userId);
    summaryQueue.getMailQueueByCategory().forEach((category, mails) -> {
      try {
        this.summarizeMails(mails, userId, category, null);
      } catch (Exception e) {
        LOG.error("Failed to send summary mail for user {}: {}", userId, e.toString());
      }
    });

    summaryQueue.getMailQueueByCategoryAndEntityId()
      .forEach((category, mailsByEntityId) -> mailsByEntityId.forEach((entityId, mails) -> {
        try {
          this.summarizeMails(mails, userId, category, entityId);
        } catch (Exception e) {
          LOG.error("Failed to send summary mail for user {}: {}", userId, e.toString());
        }
      }));

    summaryQueue.getMailQueueByCategory().clear();
    summaryQueue.getMailQueueByCategoryAndEntityId().clear();
    this.summaryQueueStore.removeAllMailsOfUser(userId);
  }

  private void summarizeMails(List<ScmMail> mails, String userId, String category, String entityId) throws MailSendBatchException {
    if (mails.isEmpty()) {
      return;
    }

    UserMailConfiguration userConfig = mailContext.getUserConfiguration(userId).orElse(new UserMailConfiguration());
    Map<String, List<Map<String, String>>> templateModel = Map.of(
      "emails",
      mails.stream().map(mail -> Map.of(
        "text", mail.getPlainText() != null ? mail.getPlainText() : "",
        "timestamp", formatCreatedAt(userConfig, mail.getCreatedAt())
      )).toList()
    );

    String englishSubject = entityId != null ?
      MessageFormat.format(SUBJECT_BUNDLES.get(ENGLISH).getString(SUMMARY_MAIL_WITH_ENTITY), category, entityId) :
      MessageFormat.format(SUBJECT_BUNDLES.get(ENGLISH).getString(SUMMARY_MAIL_WITHOUT_ENTITY), category);

    String germanSubject = entityId != null ?
      MessageFormat.format(SUBJECT_BUNDLES.get(GERMAN).getString(SUMMARY_MAIL_WITH_ENTITY), category, entityId) :
      MessageFormat.format(SUBJECT_BUNDLES.get(GERMAN).getString(SUMMARY_MAIL_WITHOUT_ENTITY), category);

    MailService.EnvelopeBuilder envelopeBuilder = envelopeBuilderSupplier.get();
    envelopeBuilder
      .toUser(userId)
      .withSubject(englishSubject)
      .withSubject(GERMAN, germanSubject)
      .withTemplate(MAIL_SUMMARY_TEMPLATE_PATH, MailTemplateType.MARKDOWN_HTML)
      .andModel(templateModel)
      .send();

    LOG.trace("Send summary mail for user {}", userId);
  }

  private String formatCreatedAt(UserMailConfiguration userConfig, LocalDateTime createdAt) {
    DateTimeFormatter formatter;

    if (userConfig.getLanguage() != null && userConfig.getLanguage().equals("de")) {
      formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy, hh:mm");
    } else {
      formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy, hh:mm");
    }

    return createdAt.format(formatter);
  }

  public synchronized void onSummarizeMailConfigChanged(SummarizeMailConfigChangedEvent event) {
    if (event.isSummarizeMailsDisabled()) {
      LOG.trace("Summary disabled for user {}", event.getUserId());

      if (sendMailTasksByUserId.containsKey(event.getUserId())) {
        Task sendMailTask = sendMailTasksByUserId.remove(event.getUserId());
        sendMailTask.cancel();
      }

      this.sendQueuedMails(event.getUserId());
    } else if (event.isFrequencyChanged()) {
      LOG.trace(
        "Sending frequency changed for user {} to {}",
        event.getUserId(),
        event.getNewUserConfig().getSummaryFrequency()
      );

      if (sendMailTasksByUserId.containsKey(event.getUserId())) {
        this.scheduleSendMailTask(event.getUserId(), event.getNewUserConfig().getSummaryFrequency().getCronExpression());
      }
    }
  }

  @Subscribe
  public synchronized void onUserDeleted(UserEvent event) {
    if (event.getEventType() != HandlerEventType.DELETE) {
      return;
    }

    summaryQueuesByUserId.remove(event.getItem().getId());
    summaryQueueStore.removeAllMailsOfUser(event.getItem().getId());
    Task task = sendMailTasksByUserId.remove(event.getItem().getId());
    if (task != null) {
      task.cancel();
    }
  }
}
