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

import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryImportEvent;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.user.User;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RepositoryImportHookTest {

  private static final Repository REPOSITORY = RepositoryTestData.createHeartOfGold();

  @Mock
  private MailService mailService;
  @Mock
  private ScmConfiguration scmConfiguration;
  @Mock(answer = Answers.RETURNS_SELF)
  private MailService.EnvelopeBuilder envelopeBuilder;
  @Mock(answer = Answers.RETURNS_SELF)
  private MailService.SubjectBuilder subjectBuilder;
  @Mock(answer = Answers.RETURNS_SELF)
  private MailService.TemplateBuilder templateBuilder;
  @Mock(answer = Answers.RETURNS_SELF)
  private MailService.MailBuilder mailBuilder;

  @Mock
  private Subject subject;
  @Mock
  private PrincipalCollection principalCollection;

  @InjectMocks
  private RepositoryImportHook hook;

  @BeforeEach
  void init() {
    ThreadContext.bind(subject);
  }

  @AfterEach
  void tearDown() {
    ThreadContext.unbindSubject();
  }

  @Test
  void shouldSendEmailForSuccess() throws MailSendBatchException {
    when(subject.getPrincipals()).thenReturn(principalCollection);
    when(principalCollection.oneByType(User.class)).thenReturn(new User());
    when(mailService.emailTemplateBuilder()).thenReturn(envelopeBuilder);
    when(envelopeBuilder.withSubject(anyString())).thenReturn(subjectBuilder);
    when(subjectBuilder.withTemplate(anyString(), any(MailTemplateType.class))).thenReturn(templateBuilder);
    when(templateBuilder.andModel(any())).thenReturn(mailBuilder);
    when(scmConfiguration.getBaseUrl()).thenReturn("https://scm-manager.org/scm");

    hook.handleEvent(new RepositoryImportEvent(REPOSITORY, false));
  }

  @Test
  void shouldSendEmailForFailed() throws MailSendBatchException {
    when(subject.getPrincipals()).thenReturn(principalCollection);
    when(principalCollection.oneByType(User.class)).thenReturn(new User());
    when(mailService.emailTemplateBuilder()).thenReturn(envelopeBuilder);
    when(envelopeBuilder.withSubject(anyString())).thenReturn(subjectBuilder);
    when(subjectBuilder.withTemplate(anyString(), any(MailTemplateType.class))).thenReturn(templateBuilder);
    when(templateBuilder.andModel(any())).thenReturn(mailBuilder);
    when(scmConfiguration.getBaseUrl()).thenReturn("https://scm-manager.org/scm");

    hook.handleEvent(new RepositoryImportEvent(REPOSITORY, true));
  }
}
