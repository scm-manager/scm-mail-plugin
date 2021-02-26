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
import sonia.scm.HandlerEventType;
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
