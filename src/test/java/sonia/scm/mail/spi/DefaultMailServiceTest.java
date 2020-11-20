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

import com.google.common.collect.ImmutableSet;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.codemonkey.simplejavamail.Email;
import org.codemonkey.simplejavamail.Mailer;
import org.codemonkey.simplejavamail.Recipient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.mail.api.Category;
import sonia.scm.mail.api.MailConfiguration;
import sonia.scm.mail.api.MailContext;
import sonia.scm.mail.api.MailSendBatchException;
import sonia.scm.mail.api.MailService;
import sonia.scm.mail.api.MailTemplateType;
import sonia.scm.mail.api.Topic;
import sonia.scm.mail.api.UserMailConfiguration;
import sonia.scm.mail.spi.content.MailContent;
import sonia.scm.mail.spi.content.MailContentRenderer;
import sonia.scm.mail.spi.content.MailContentRendererFactory;
import sonia.scm.trace.Span;
import sonia.scm.trace.Tracer;
import sonia.scm.user.DisplayUser;
import sonia.scm.user.User;
import sonia.scm.user.UserDisplayManager;
import sonia.scm.user.UserTestData;

import java.io.IOException;
import java.util.Locale;

import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultMailServiceTest {


  @Mock
  private MailContext context;

  @Mock
  private UserDisplayManager userDisplayManager;

  @Mock
  private MailContentRendererFactory mailContentRendererFactory;

  @Mock
  private MailContentRenderer mailContentRenderer;

  @Mock
  private MailConfiguration configuration;

  @Mock
  private Tracer tracer;
  @Mock
  private Span span;

  @Mock
  private Mailer mailer;

  private MailService mailService;


  @Captor
  private ArgumentCaptor<Email> emailCaptor;

  @BeforeEach
  void setUpMocks() {
    this.mailService = new TestingMailService();
    when(context.getConfiguration()).thenReturn(configuration);
    lenient().doNothing().when(mailer).sendMail(emailCaptor.capture());
    lenient().when(tracer.span("Mail")).thenReturn(span);
  }

  @Test
  void shouldSendEmail() throws MailSendBatchException, IOException {
    configureMailer();
    mockContentRenderer(Locale.ENGLISH, "my-template", "model", "Don't Panic");

    mailService.emailTemplateBuilder()
      .toAddress(Locale.ENGLISH, "Tricia McMillan", "tricia.mcmillan@hitchhiker.com")
      .withSubject("Hello World")
      .withTemplate("my-template", MailTemplateType.TEXT)
      .andModel("model")
      .send();

    Email email = emailCaptor.getValue();

    assertRecipient(email, "Tricia McMillan", "tricia.mcmillan@hitchhiker.com");

    assertThat(email.getSubject()).isEqualTo("Hello World");
    assertThat(email.getText()).isEqualTo("Don't Panic");
  }

  @Test
  void shouldTraceCall() throws MailSendBatchException, IOException {
    configureMailer();
    when(configuration.getHost()).thenReturn("marvin");
    when(configuration.getPort()).thenReturn(42);
    mockContentRenderer(Locale.ENGLISH, "my-template", "model", "Don't Panic");

    mailService.emailTemplateBuilder()
      .toAddress(Locale.ENGLISH, "Tricia McMillan", "tricia.mcmillan@hitchhiker.com")
      .withSubject("Hello World")
      .withTemplate("my-template", MailTemplateType.TEXT)
      .andModel("model")
      .send();

    verify(span).label("url", "marvin:42");
    verify(span).label("method", "SMTP");
  }

  @Test
  void shouldSendEmailAndResolveUser() throws IOException, MailSendBatchException {
    configureMailer();
    mockUser(UserTestData.createTrillian());
    mockContentRenderer(Locale.ENGLISH, "my-template", "model", "Don't Panic");

    mailService.emailTemplateBuilder()
      .toUser("trillian")
      .withSubject("Hello Tricia")
      .withTemplate("my-template", MailTemplateType.TEXT)
      .andModel("model")
      .send();

    Email email = emailCaptor.getValue();

    assertRecipient(email, "Tricia McMillan", "tricia.mcmillan@hitchhiker.com");
    assertThat(email.getSubject()).isEqualTo("Hello Tricia");
    assertThat(email.getText()).isEqualTo("Don't Panic");
  }

  @Test
  void shouldSendEmailAndUseLocaleOfUser() throws IOException, MailSendBatchException {
    configureMailer();
    mockContentRenderer(Locale.GERMAN, "my-template", "model", "Keine Panik");
    mockUserWithConfiguration(UserTestData.createTrillian(), Locale.GERMAN);

    mailService.emailTemplateBuilder()
      .toUser("trillian")
      .withSubject("Hello Tricia")
      .withSubject(Locale.GERMAN, "Hallo Tricia")
      .withTemplate("my-template", MailTemplateType.TEXT)
      .andModel("model")
      .send();

    Email email = emailCaptor.getValue();

    assertThat(email.getSubject()).isEqualTo("Hallo Tricia");
    assertThat(email.getText()).isEqualTo("Keine Panik");
  }

  @Test
  void shouldNotSendEmailForUnknownUsers() throws MailSendBatchException, IOException {
    when(configuration.isValid()).thenReturn(Boolean.TRUE);

    mailService.emailTemplateBuilder()
      .toUser("trillian")
      .withSubject("Hello Tricia")
      .withTemplate("my-template", MailTemplateType.TEXT)
      .andModel("model")
      .send();

    verify(mailer, never()).sendMail(any(Email.class));
  }

  @Test
  void shouldSendEmailAndUseLocaleFromConfiguration() throws MailSendBatchException, IOException {
    mockContentRenderer(Locale.GERMAN, "my-template", "model", "Keine Panik");

    MailConfiguration config = mock(MailConfiguration.class);
    when(config.isValid()).thenReturn(Boolean.TRUE);
    when(config.getLanguage()).thenReturn("de");
    when(mailer.validate(any(Email.class))).thenReturn(Boolean.TRUE);

    mailService.emailTemplateBuilder()
      .withConfiguration(config)
      .toAddress("tricia.mcmillan@hitchhiker.com")
      .withSubject("Hello Tricia")
      .withSubject(Locale.GERMAN, "Hallo Tricia")
      .withTemplate("my-template", MailTemplateType.TEXT)
      .andModel("model")
      .send();

    Email email = emailCaptor.getValue();

    assertThat(email.getSubject()).isEqualTo("Hallo Tricia");
    assertThat(email.getText()).isEqualTo("Keine Panik");
  }

  @Test
  void shouldSendEmailAndUseConfiguredFrom() throws MailSendBatchException, IOException {
    configureMailer();
    mockContentRenderer(Locale.ENGLISH, "my-template", "model", "Don't Panic");

    mailService.emailTemplateBuilder()
      .from("Arthur Dent")
      .toAddress(Locale.ENGLISH, "Tricia McMillan", "tricia.mcmillan@hitchhiker.com")
      .withSubject("Hello World")
      .withTemplate("my-template", MailTemplateType.TEXT)
      .andModel("model")
      .send();

    Email email = emailCaptor.getValue();
    Recipient fromRecipient = email.getFromRecipient();
    assertThat(fromRecipient.getName()).isEqualTo("Arthur Dent");
  }

  @Test
  void shouldSendEmailAndUseFromFromSubject() throws MailSendBatchException, IOException {
    configureMailer();
    mockContentRenderer(Locale.ENGLISH, "my-template", "model", "Don't Panic");

    SimplePrincipalCollection principals = new SimplePrincipalCollection();
    principals.add(UserTestData.createDent(), "test");

    Subject subject = mock(Subject.class);
    when(subject.getPrincipals()).thenReturn(principals);
    ThreadContext.bind(subject);

    try {
      mailService.emailTemplateBuilder()
        .fromCurrentUser()
        .toAddress(Locale.ENGLISH, "Tricia McMillan", "tricia.mcmillan@hitchhiker.com")
        .withSubject("Hello World")
        .withTemplate("my-template", MailTemplateType.TEXT)
        .andModel("model")
        .send();

    } finally {
      ThreadContext.unbindSubject();
    }

    Email email = emailCaptor.getValue();
    Recipient fromRecipient = email.getFromRecipient();
    assertThat(fromRecipient.getName()).isEqualTo("Arthur Dent");
  }

  @Test
  void shouldSendHtmlAndTextEmail() throws MailSendBatchException, IOException {
    configureMailer();
    MailContent mailContent = MailContent.textAndHtml("Don't Panic", "<h1>Don't Panic</h1>");
    mockContentRenderer(Locale.ENGLISH, "my-template", "model", MailTemplateType.MARKDOWN_HTML, mailContent);

    mailService.emailTemplateBuilder()
      .toAddress(Locale.ENGLISH, "Tricia McMillan", "tricia.mcmillan@hitchhiker.com")
      .withSubject("Hello World")
      .withTemplate("my-template", MailTemplateType.MARKDOWN_HTML)
      .andModel("model")
      .send();

    Email email = emailCaptor.getValue();

    assertRecipient(email, "Tricia McMillan", "tricia.mcmillan@hitchhiker.com");

    assertThat(email.getSubject()).isEqualTo("Hello World");
    assertThat(email.getText()).isEqualTo("Don't Panic");
    assertThat(email.getTextHTML()).isEqualTo("<h1>Don't Panic</h1>");
  }

  @Test
  void shouldNotSendEmailToUserWhoExcludedTopic() throws IOException, MailSendBatchException {
    Topic excludedTopic = new Topic(new Category("boring"), "stuff");
    mockUserWithConfiguration(UserTestData.createTrillian(), Locale.ENGLISH, excludedTopic);
    mockContentRenderer(Locale.ENGLISH, "my-template", "model", "Don't Panic");

    mailService.emailTemplateBuilder()
      .toUser("trillian")
      .onTopic(excludedTopic)
      .withSubject("Hello Tricia")
      .withTemplate("my-template", MailTemplateType.TEXT)
      .andModel("model")
      .send();

    assertThat(emailCaptor.getAllValues()).isEmpty();
  }

  @Test
  void shouldSendEmailToUserWhenOtherTopicExcluded() throws IOException, MailSendBatchException {
    configureMailer();
    Topic excludedTopic = new Topic(new Category("boring"), "stuff");
    mockUserWithConfiguration(UserTestData.createTrillian(), Locale.ENGLISH, excludedTopic);
    mockContentRenderer(Locale.ENGLISH, "my-template", "model", "Don't Panic");

    mailService.emailTemplateBuilder()
      .toUser("trillian")
      .onTopic(new Topic(new Category("mice"), "the question"))
      .withSubject("Hello Tricia")
      .withTemplate("my-template", MailTemplateType.TEXT)
      .andModel("model")
      .send();

    assertThat(emailCaptor.getAllValues()).isNotEmpty();
  }

  private void mockUser(User user) {
    lenient().when(userDisplayManager.get(user.getId())).thenReturn(of(DisplayUser.from(user)));
  }

  private void mockUserWithConfiguration(User user, Locale locale, Topic... excludedTopics) {
    mockUser(user);

    UserMailConfiguration userMailConfiguration = new UserMailConfiguration();
    userMailConfiguration.setLanguage(locale.getLanguage());
    if (excludedTopics.length > 0) {
      userMailConfiguration.setExcludedTopics(ImmutableSet.copyOf(excludedTopics));
    }
    when(context.getUserConfiguration(user.getId())).thenReturn(of(userMailConfiguration));
  }

  private void assertRecipient(Email email, String displayName, String address) {
    Recipient recipient = email.getRecipients().get(0);
    assertThat(recipient.getName()).isEqualTo(displayName);
    assertThat(recipient.getAddress()).isEqualTo(address);
  }

  private void mockContentRenderer(Locale locale, String template, Object model, String result) throws IOException {
    mockContentRenderer(locale, template, model, MailTemplateType.TEXT, MailContent.text(result));
  }

  private void mockContentRenderer(Locale locale, String template, Object model, MailTemplateType type, MailContent content) throws IOException {
    lenient().when(mailContentRendererFactory.createMailContentRenderer(template, type)).thenReturn(mailContentRenderer);
    lenient().when(mailContentRenderer.createMailContent(locale, model)).thenReturn(content);
  }

  private void configureMailer() {
    when(configuration.isValid()).thenReturn(Boolean.TRUE);
    when(mailer.validate(any(Email.class))).thenReturn(Boolean.TRUE);
  }

  public class TestingMailService extends DefaultMailService {
    private TestingMailService() {
      super(context, userDisplayManager, mailContentRendererFactory, tracer);
    }

    @Override
    Mailer createMailer(MailConfiguration configuration) {
      return mailer;
    }
  }

}
