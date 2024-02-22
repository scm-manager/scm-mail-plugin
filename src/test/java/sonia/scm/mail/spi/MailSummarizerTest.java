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

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.HandlerEventType;
import sonia.scm.mail.api.MailContext;
import sonia.scm.mail.api.MailSendBatchException;
import sonia.scm.mail.api.MailService;
import sonia.scm.mail.api.MailTemplateType;
import sonia.scm.mail.api.ScmMail;
import sonia.scm.mail.api.ScmRecipient;
import sonia.scm.mail.api.SummarizeMailConfigChangedEvent;
import sonia.scm.mail.api.SummaryFrequency;
import sonia.scm.mail.api.UserMailConfiguration;
import sonia.scm.schedule.Scheduler;
import sonia.scm.schedule.Task;
import sonia.scm.user.User;
import sonia.scm.user.UserEvent;
import sonia.scm.user.UserTestData;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class})
class MailSummarizerTest {

  @Mock
  private MailService mailService;

  @Mock(answer = Answers.RETURNS_SELF)
  private MailService.EnvelopeBuilder envelopeBuilder;

  @Mock
  private MailService.SubjectBuilder subjectBuilder;

  @Mock
  private MailService.TemplateBuilder templateBuilder;

  @Mock
  private MailService.MailBuilder mailBuilder;

  @Mock
  private MailContext mailContext;

  @Mock
  private MailSummaryQueueStore summaryQueueStore;

  @Mock
  private Scheduler scheduler;

  @Mock
  private Supplier<MailService.EnvelopeBuilder> envelopeBuilderSupplier;

  @InjectMocks
  private MailSummarizer mailSummaryService;

  @Nested
  public class AddMail {

    @Test
    void shouldSendMailDirectlyBecauseMailSummaryIsDisabled() throws MailSendBatchException {
      String userId = "userId";
      ScmMail mail = new ScmMail();
      mail.setFrom(new ScmRecipient("Test", "test@test.com"));
      mail.setSubject("Test Subject");
      mail.setPlainText("Test text");

      UserMailConfiguration userMailConfiguration = new UserMailConfiguration();
      userMailConfiguration.setSummarizeMails(false);
      when(mailContext.getUserConfiguration(userId)).thenReturn(Optional.of(userMailConfiguration));

      when(envelopeBuilderSupplier.get()).thenReturn(envelopeBuilder);
      when(envelopeBuilder.withSubject(mail.getSubject())).thenReturn(subjectBuilder);
      when(subjectBuilder.withTemplate("sonia/scm/mail/emailnotification/single_queued_mail.mustache", MailTemplateType.MARKDOWN_HTML))
        .thenReturn(templateBuilder);
      when(templateBuilder.andModel(any(Object.class))).thenReturn(mailBuilder);

      mailSummaryService.addMail(userId, "pullRequest", "1", mail);

      verify(mailContext).getUserConfiguration(userId);
      verify(mailBuilder).send();

      assertThat(mailSummaryService.getSummaryQueuesByUserId().size()).isEqualTo(0);
    }

    @Test
    void shouldQueueMailsByCategoryBecauseEntitySummaryIsDisabled() throws MailSendBatchException {
      String pullRequestCategory = "pullRequest";
      String mirrorCategory = "mirror";

      String firstUser = "user1";
      ScmMail firstPullRequestMail = new ScmMail();
      ScmMail secondPullRequestMail = new ScmMail();
      ScmMail firstMirrorMail = new ScmMail();

      String secondUser = "user2";
      ScmMail thirdPullRequestMail = new ScmMail();
      ScmMail secondMirrorMail = new ScmMail();

      UserMailConfiguration userMailConfiguration = new UserMailConfiguration();
      userMailConfiguration.setSummarizeMails(true);
      userMailConfiguration.setSummarizeByEntity(false);
      userMailConfiguration.setSummaryFrequency(SummaryFrequency.HOURS_2);
      when(mailContext.getUserConfiguration(firstUser)).thenReturn(Optional.of(userMailConfiguration));
      when(mailContext.getUserConfiguration(secondUser)).thenReturn(Optional.of(userMailConfiguration));

      mailSummaryService.addMail(firstUser, pullRequestCategory, "1", firstPullRequestMail);
      mailSummaryService.addMail(firstUser, pullRequestCategory, "1", secondPullRequestMail);
      mailSummaryService.addMail(secondUser, pullRequestCategory, "1", thirdPullRequestMail);

      mailSummaryService.addMail(firstUser, mirrorCategory, "1", firstMirrorMail);
      mailSummaryService.addMail(secondUser, mirrorCategory, "1", secondMirrorMail);

      verify(mailContext, times(3)).getUserConfiguration(firstUser);
      verify(mailContext, times(2)).getUserConfiguration(secondUser);
      verify(scheduler, times(2)).schedule(
        eq(SummaryFrequency.HOURS_2.getCronExpression()), any(Runnable.class)
      );
      verifyNoInteractions(mailService);

      assertThat(mailSummaryService.getSummaryQueuesByUserId().get(firstUser).getMailQueueByCategory())
        .isEqualTo(
          Map.of(
            pullRequestCategory, List.of(firstPullRequestMail, secondPullRequestMail),
            mirrorCategory, List.of(firstMirrorMail)
          )
        );
      assertThat(mailSummaryService.getSummaryQueuesByUserId().get(secondUser).getMailQueueByCategory())
        .isEqualTo(
          Map.of(
            pullRequestCategory, List.of(thirdPullRequestMail),
            mirrorCategory, List.of(secondMirrorMail)
          )
        );
    }

    @Test
    void shouldQueueMailsByCategoryAndEntity() throws MailSendBatchException {
      String pullRequestCategory = "pullRequest";
      String firstPullRequestId = "1";
      String secondPullRequestId = "2";
      String mirrorCategory = "mirror";
      String firstMirrorId = "3";

      String firstUser = "user1";
      ScmMail firstPullRequestMail = new ScmMail();
      ScmMail secondPullRequestMail = new ScmMail();
      ScmMail firstMirrorMail = new ScmMail();

      String secondUser = "user2";
      ScmMail thirdPullRequestMail = new ScmMail();
      ScmMail secondMirrorMail = new ScmMail();

      UserMailConfiguration userMailConfiguration = new UserMailConfiguration();
      userMailConfiguration.setSummarizeMails(true);
      userMailConfiguration.setSummarizeByEntity(true);
      userMailConfiguration.setSummaryFrequency(SummaryFrequency.HOURS_2);
      when(mailContext.getUserConfiguration(firstUser)).thenReturn(Optional.of(userMailConfiguration));
      when(mailContext.getUserConfiguration(secondUser)).thenReturn(Optional.of(userMailConfiguration));

      mailSummaryService.addMail(firstUser, pullRequestCategory, firstPullRequestId, firstPullRequestMail);
      mailSummaryService.addMail(firstUser, pullRequestCategory, secondPullRequestId, secondPullRequestMail);
      mailSummaryService.addMail(secondUser, pullRequestCategory, firstPullRequestId, thirdPullRequestMail);

      mailSummaryService.addMail(firstUser, mirrorCategory, firstMirrorId, firstMirrorMail);
      mailSummaryService.addMail(secondUser, mirrorCategory, firstMirrorId, secondMirrorMail);

      verify(mailContext, times(3)).getUserConfiguration(firstUser);
      verify(mailContext, times(2)).getUserConfiguration(secondUser);
      verify(scheduler, times(2)).schedule(
        eq(SummaryFrequency.HOURS_2.getCronExpression()), any(Runnable.class)
      );
      verifyNoInteractions(mailService);

      assertThat(mailSummaryService.getSummaryQueuesByUserId().get(firstUser).getMailQueueByCategoryAndEntityId())
        .isEqualTo(
          Map.of(
            pullRequestCategory, Map.of(
              firstPullRequestId, List.of(firstPullRequestMail),
              secondPullRequestId, List.of(secondPullRequestMail)
            ),
            mirrorCategory, Map.of(firstMirrorId, List.of(firstMirrorMail))
          )
        );
      assertThat(mailSummaryService.getSummaryQueuesByUserId().get(secondUser).getMailQueueByCategoryAndEntityId())
        .isEqualTo(
          Map.of(
            pullRequestCategory, Map.of(firstPullRequestId, List.of(thirdPullRequestMail)),
            mirrorCategory, Map.of(firstMirrorId, List.of(secondMirrorMail))
          )
        );
    }
  }

  @Test
  void shouldNotCreateSendMailTaskForDisabledSummary() {
    when(summaryQueueStore.getAll()).thenReturn(Map.of("userId", new MailSummaryQueue()));
    UserMailConfiguration userConfig = new UserMailConfiguration();
    userConfig.setSummarizeMails(false);
    when(mailContext.getUserConfiguration("userId")).thenReturn(Optional.of(userConfig));

    new MailSummarizer(summaryQueueStore, mailService::emailTemplateBuilder, mailContext, scheduler);

    verifyNoInteractions(scheduler);
  }

  @Test
  void shouldCreateSendMailTask() {
    when(summaryQueueStore.getAll()).thenReturn(Map.of("userId", new MailSummaryQueue()));
    UserMailConfiguration userConfig = new UserMailConfiguration();
    userConfig.setSummarizeMails(true);
    userConfig.setSummaryFrequency(SummaryFrequency.HOURS_2);
    when(mailContext.getUserConfiguration("userId")).thenReturn(Optional.of(userConfig));

    new MailSummarizer(summaryQueueStore, mailService::emailTemplateBuilder, mailContext, scheduler);

    verify(scheduler).schedule(eq(userConfig.getSummaryFrequency().getCronExpression()), any(Runnable.class));
  }

  @Test
  void shouldCreateNewSendMailTaskAndCancelPrevious() {
    when(summaryQueueStore.getAll()).thenReturn(Map.of("userId", new MailSummaryQueue()));

    UserMailConfiguration userConfig = new UserMailConfiguration();
    userConfig.setSummarizeMails(true);
    userConfig.setSummaryFrequency(SummaryFrequency.HOURS_2);
    when(mailContext.getUserConfiguration("userId")).thenReturn(Optional.of(userConfig));

    UserMailConfiguration newConfig = new UserMailConfiguration();
    newConfig.setSummarizeMails(true);
    newConfig.setSummaryFrequency(SummaryFrequency.MINUTES_15);

    Task task = mock(Task.class);
    when(scheduler.schedule(eq(userConfig.getSummaryFrequency().getCronExpression()), any(Runnable.class))).thenReturn(task);

    MailSummarizer mailSummaryService = new MailSummarizer(summaryQueueStore, mailService::emailTemplateBuilder, mailContext, scheduler);
    mailSummaryService.onSummarizeMailConfigChanged(
      new SummarizeMailConfigChangedEvent(
        "userId", userConfig, newConfig
      )
    );

    verify(task).cancel();
    verify(scheduler).schedule(eq(SummaryFrequency.MINUTES_15.getCronExpression()), any(Runnable.class));
  }

  @Test
  void shouldNotUpdateAnything() {
    when(summaryQueueStore.getAll()).thenReturn(Map.of("userId", new MailSummaryQueue()));

    UserMailConfiguration userConfig = new UserMailConfiguration();
    userConfig.setSummarizeMails(true);
    userConfig.setSummaryFrequency(SummaryFrequency.HOURS_2);
    when(mailContext.getUserConfiguration("userId")).thenReturn(Optional.of(userConfig));

    UserMailConfiguration newConfig = new UserMailConfiguration();
    newConfig.setSummarizeMails(true);
    newConfig.setSummaryFrequency(SummaryFrequency.HOURS_2);

    MailSummarizer mailSummaryService = new MailSummarizer(summaryQueueStore, mailService::emailTemplateBuilder, mailContext, scheduler);
    mailSummaryService.onSummarizeMailConfigChanged(
      new SummarizeMailConfigChangedEvent(
        "userId", userConfig, newConfig
      )
    );

    verify(scheduler).schedule(eq(userConfig.getSummaryFrequency().getCronExpression()), any(Runnable.class));
    verifyNoMoreInteractions(scheduler);
  }

  @Test
  void shouldCancelTaskAndSendOutMails() {
    ScmMail mail = new ScmMail(
      "pull-rquest",
      null,
      new ScmRecipient("From", "from@email.com"),
      new ScmRecipient("to", "to@email.com"),
      "subject",
      "text"
    );
    MailSummaryQueue summaryQueue = new MailSummaryQueue();
    summaryQueue.add("pull-request", mail);
    summaryQueue.add("pull-request", "1", mail);
    when(summaryQueueStore.getAll()).thenReturn(Map.of("userId", summaryQueue));

    UserMailConfiguration userConfig = new UserMailConfiguration();
    userConfig.setSummarizeMails(true);
    userConfig.setSummaryFrequency(SummaryFrequency.HOURS_2);
    when(mailContext.getUserConfiguration("userId")).thenReturn(Optional.of(userConfig));

    UserMailConfiguration newConfig = new UserMailConfiguration();
    newConfig.setSummarizeMails(false);
    newConfig.setSummaryFrequency(SummaryFrequency.HOURS_8);

    Task task = mock(Task.class);
    when(scheduler.schedule(eq(userConfig.getSummaryFrequency().getCronExpression()), any(Runnable.class))).thenReturn(task);

    MailSummarizer mailSummaryService = new MailSummarizer(summaryQueueStore, mailService::emailTemplateBuilder, mailContext, scheduler);
    mailSummaryService.onSummarizeMailConfigChanged(
      new SummarizeMailConfigChangedEvent(
        "userId", userConfig, newConfig)
    );

    verify(task).cancel();
    verify(scheduler).schedule(eq(userConfig.getSummaryFrequency().getCronExpression()), any(Runnable.class));
    verifyNoMoreInteractions(scheduler);
  }

  @Test
  void shouldHandleDeletedUser() throws MailSendBatchException {
    User deletedUser = UserTestData.createAdams();

    UserMailConfiguration userConfig = new UserMailConfiguration();
    userConfig.setSummarizeMails(true);
    userConfig.setSummaryFrequency(SummaryFrequency.HOURS_2);
    when(mailContext.getUserConfiguration(deletedUser.getId())).thenReturn(Optional.of(userConfig));

    Task task = mock(Task.class);
    when(scheduler.schedule(eq(userConfig.getSummaryFrequency().getCronExpression()), any(Runnable.class))).thenReturn(task);

    MailSummarizer mailSummaryService = new MailSummarizer(summaryQueueStore, mailService::emailTemplateBuilder, mailContext, scheduler);
    mailSummaryService.addMail(deletedUser.getId(), "test", null, new ScmMail());

    mailSummaryService.onUserDeleted(new UserEvent(HandlerEventType.DELETE, deletedUser));

    assertThat(mailSummaryService.getSummaryQueuesByUserId().get(deletedUser.getId())).isNull();
    verify(summaryQueueStore).removeAllMailsOfUser(deletedUser.getId());
    verify(task).cancel();
  }

  @Test
  void shouldIgnoreOtherUserEvent() throws MailSendBatchException {
    User modifiedUser = UserTestData.createAdams();

    UserMailConfiguration userConfig = new UserMailConfiguration();
    userConfig.setSummarizeMails(true);
    userConfig.setSummaryFrequency(SummaryFrequency.HOURS_2);
    when(mailContext.getUserConfiguration(modifiedUser.getId())).thenReturn(Optional.of(userConfig));

    MailSummarizer mailSummaryService = new MailSummarizer(summaryQueueStore, mailService::emailTemplateBuilder, mailContext, scheduler);
    mailSummaryService.addMail(modifiedUser.getId(), "test", null, new ScmMail());

    mailSummaryService.onUserDeleted(new UserEvent(HandlerEventType.MODIFY, modifiedUser));

    assertThat(mailSummaryService.getSummaryQueuesByUserId().get(modifiedUser.getId())).isNotNull();
  }
}
