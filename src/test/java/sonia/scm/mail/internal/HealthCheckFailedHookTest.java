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

package sonia.scm.mail.internal;

import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.mail.api.MailSendBatchException;
import sonia.scm.mail.api.MailService;
import sonia.scm.mail.api.MailTemplateType;
import sonia.scm.repository.HealthCheckEvent;
import sonia.scm.repository.HealthCheckFailure;
import sonia.scm.repository.Repository;
import sonia.scm.user.DisplayUser;
import sonia.scm.user.User;
import sonia.scm.user.UserDisplayManager;

import java.util.Collections;

import static java.util.Optional.of;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class HealthCheckFailedHookTest {

  private final Repository REPOSITORY = new Repository("1", "git", "undermore", "repo");
  private final HealthCheckFailure healthCheckFailure = new HealthCheckFailure("1", "vogons", "met vogons");

  @Mock
  private MailService mailService;

  @Mock
  private ScmConfiguration scmConfiguration;

  @Mock
  private UserDisplayManager userDisplayManager;

  @Mock(answer = Answers.RETURNS_SELF)
  private MailService.EnvelopeBuilder envelopeBuilder;
  @Mock(answer = Answers.RETURNS_SELF)
  private MailService.SubjectBuilder subjectBuilder;
  @Mock(answer = Answers.RETURNS_SELF)
  private MailService.TemplateBuilder templateBuilder;
  @Mock(answer = Answers.RETURNS_SELF)
  private MailService.MailBuilder mailBuilder;

  @InjectMocks
  private HealthCheckFailedHook healthCheckFailedHook;

  @Test
  void shouldSendMailForHealthChange() throws MailSendBatchException {
    when(mailService.emailTemplateBuilder()).thenReturn(envelopeBuilder);
    when(envelopeBuilder.withSubject(anyString())).thenReturn(subjectBuilder);
    when(subjectBuilder.withTemplate(anyString(), any(MailTemplateType.class))).thenReturn(templateBuilder);
    when(templateBuilder.andModel(any())).thenReturn(mailBuilder);
    when(scmConfiguration.getEmergencyContacts()).thenReturn(ImmutableSet.of("name"));
    when(userDisplayManager.get("name")).thenReturn(of(DisplayUser.from(new User("name", "name", "name@mail.com"))));

    HealthCheckEvent healthCheckEvent = new HealthCheckEvent(REPOSITORY, Collections.emptyList(), Collections.singletonList(healthCheckFailure));
    healthCheckFailedHook.handle(healthCheckEvent);

    verify(mailService).emailTemplateBuilder();
  }

  @Test
  void shouldNotSendMail() throws MailSendBatchException {
    HealthCheckEvent healthCheckEvent = new HealthCheckEvent(REPOSITORY, Collections.emptyList(), Collections.emptyList());
    healthCheckFailedHook.handle(healthCheckEvent);

    verify(mailService, never()).emailTemplateBuilder();
  }
}
