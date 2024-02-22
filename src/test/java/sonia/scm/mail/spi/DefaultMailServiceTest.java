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
import jakarta.inject.Provider;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.simplejavamail.api.email.Email;
import org.simplejavamail.api.email.Recipient;
import org.simplejavamail.api.mailer.Mailer;
import sonia.scm.mail.api.Category;
import sonia.scm.mail.api.MailConfiguration;
import sonia.scm.mail.api.MailContext;
import sonia.scm.mail.api.MailSendBatchException;
import sonia.scm.mail.api.MailService;
import sonia.scm.mail.api.MailTemplateType;
import sonia.scm.mail.api.ScmRecipient;
import sonia.scm.mail.api.ScmTransportStrategy;
import sonia.scm.mail.api.Topic;
import sonia.scm.mail.api.UserMailConfiguration;
import sonia.scm.mail.spi.content.MailContent;
import sonia.scm.mail.spi.content.MailContentRenderer;
import sonia.scm.mail.spi.content.MailContentRendererFactory;
import sonia.scm.schedule.Scheduler;
import sonia.scm.trace.Span;
import sonia.scm.trace.Tracer;
import sonia.scm.user.DisplayUser;
import sonia.scm.user.User;
import sonia.scm.user.UserDisplayManager;
import sonia.scm.user.UserTestData;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
  private Provider<SSLContext> sslContextProvider;

  @Mock
  private MailSummaryQueueStore summaryQueueStore;

  @Mock
  private Scheduler scheduler;

  @Mock
  private Tracer tracer;
  @Mock
  private Span span;

  @Mock
  private Mailer mailer;

  private MailService mailService;

  @Captor
  private ArgumentCaptor<Email> emailCaptor;

  @Captor
  private ArgumentCaptor<Iterable<Email>> multipleEmailsCaptor;

  @BeforeEach
  void setUpMocks() {
    this.mailService = new TestingMailService();
    lenient().when(context.getConfiguration()).thenReturn(configuration);
    lenient().when(mailer.sendMail(emailCaptor.capture())).thenReturn(CompletableFuture.completedFuture(null));
    lenient().when(tracer.span("Mail")).thenReturn(span);
  }

  @Test
  void shouldSendEmail() throws MailSendBatchException {
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
    assertThat(email.getPlainText()).isEqualTo("Don't Panic");
  }

  @Test
  void shouldTraceCall() throws MailSendBatchException {
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
  void shouldSendEmailAndResolveUser() throws MailSendBatchException {
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
    assertThat(email.getPlainText()).isEqualTo("Don't Panic");
  }

  @Test
  void shouldSendEmailAndUseLocaleOfUser() throws MailSendBatchException {
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
    assertThat(email.getPlainText()).isEqualTo("Keine Panik");
  }

  @Test
  void shouldNotSendEmailForUnknownUsers() throws MailSendBatchException {
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
  void shouldSendEmailAndUseLocaleFromConfiguration() throws MailSendBatchException {
    mockContentRenderer(Locale.GERMAN, "my-template", "model", "Keine Panik");

    MailConfiguration config = mock(MailConfiguration.class);
    when(config.getFrom()).thenReturn("test@test.de");
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
    assertThat(email.getPlainText()).isEqualTo("Keine Panik");
  }

  @Test
  void shouldSendEmailAndUseConfiguredFrom() throws MailSendBatchException {
    configureMailer();
    when(configuration.getFrom()).thenReturn("test@test.de");
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
  void shouldSendEmailAndUseFromFromSubject() throws MailSendBatchException {
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
  void shouldSendHtmlAndTextEmail() throws MailSendBatchException {
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
    assertThat(email.getPlainText()).isEqualTo("Don't Panic");
    assertThat(email.getHTMLText()).isEqualTo("<h1>Don't Panic</h1>");
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

  @Test
  void shouldSetCustomSSLSocketFactory() {
    SSLContext sslContext = mock(SSLContext.class);
    when(sslContextProvider.get()).thenReturn(sslContext);
    SSLSocketFactory socketFactory = mock(SSLSocketFactory.class);
    when(sslContext.getSocketFactory()).thenReturn(socketFactory);

    DefaultMailService service = new DefaultMailService(
      context, userDisplayManager, mailContentRendererFactory, new MailSender(tracer, sslContextProvider), summaryQueueStore, scheduler
    );

    Mailer mailer = service
      .getMailSender()
      .createMailer(
        new MailConfiguration("host", 443, ScmTransportStrategy.SMTPS, "trillian", "Testmail")
      );

    Properties props = mailer.getSession().getProperties();
    assertThat(props)
      .containsEntry("mail.smtp.ssl.socketFactory", socketFactory)
      .containsEntry("mail.smtps.ssl.socketFactory", socketFactory);
  }

  @Test
  void shouldBuildEmailsForRecipients() throws MailSendBatchException {
    mockContentRenderer(Locale.ENGLISH, "my-template", "model", "TEST CONTENT");

    Topic topic = new Topic(new Category("Pull Request"), "Mentions");

    User trillian = UserTestData.createTrillian();
    mockUserWithConfiguration(trillian, Locale.ENGLISH, topic);

    User dent = UserTestData.createDent();
    mockUserWithConfiguration(dent, Locale.ENGLISH);

    User adams = UserTestData.createAdams();
    mockUserWithConfiguration(adams, Locale.ENGLISH);

    DefaultMailService service = new DefaultMailService(
      context, userDisplayManager, mailContentRendererFactory, new MailSender(tracer, sslContextProvider), summaryQueueStore, scheduler
    );
    MailService.EnvelopeBuilder envelopeBuilder = service.emailTemplateBuilder();
    List.of(trillian, dent, adams).forEach(user -> envelopeBuilder.toUser(user.getId()));
    envelopeBuilder
      .onTopic(topic)
      .onEntity("1")
      .withSubject("Mentioned")
      .withTemplate("my-template", MailTemplateType.TEXT)
      .andModel("model")
      .queueMails();

    assertThat(service.getMailSummarizer().getSummaryQueuesByUserId().keySet()).isEqualTo(
      Set.of(dent.getId(), adams.getId())
    );
  }

  @Test
  void shouldSendMailDirectlyAndSetProperFrom() throws MailSendBatchException {
    mockContentRenderer(Locale.ENGLISH, "my-template", "model", "TEST CONTENT");
    mockContentRenderer(
      Locale.ENGLISH,
      "sonia/scm/mail/emailnotification/single_queued_mail.mustache",
      Map.of("content", "TEST CONTENT"),
      MailTemplateType.MARKDOWN_HTML,
      MailContent.text("TEST CONTENT")
    );

    ScmRecipient from = new ScmRecipient("Test", "test@test.com");
    Topic topic = new Topic(new Category("Pull Request"), "Mentions");

    User trillian = UserTestData.createTrillian();
    mockUser(trillian);
    UserMailConfiguration userMailConfiguration = new UserMailConfiguration();
    userMailConfiguration.setSummarizeMails(false);
    userMailConfiguration.setLanguage(Locale.ENGLISH.getLanguage());
    when(context.getUserConfiguration(trillian.getId())).thenReturn(Optional.of(userMailConfiguration));

    MailSender mailSender = mock(MailSender.class);
    DefaultMailService service = new DefaultMailService(
      context, userDisplayManager, mailContentRendererFactory, mailSender, summaryQueueStore, scheduler
    );
    MailService.EnvelopeBuilder envelopeBuilder = service.emailTemplateBuilder();
    envelopeBuilder
      .from(from)
      .toUser(trillian.getId())
      .onTopic(topic)
      .onEntity("1")
      .withSubject("Mentioned")
      .withTemplate("my-template", MailTemplateType.TEXT)
      .andModel("model")
      .queueMails();

    assertThat(service.getMailSummarizer().getSummaryQueuesByUserId()).hasSize(0);
    verify(mailSender).send(any(MailConfiguration.class), multipleEmailsCaptor.capture());
    List<Email> capturedEmails = new ArrayList<>();
    multipleEmailsCaptor.getValue().forEach(capturedEmails::add);

    assertThat(capturedEmails).hasSize(1);
    assertThat(capturedEmails.get(0).getFromRecipient().getName()).isEqualTo(from.getDisplayName());
    assertThat(capturedEmails.get(0).getFromRecipient().getAddress()).isEqualTo(from.getAddress());
  }

  private void mockUser(User user) {
    lenient().when(userDisplayManager.get(user.getId())).thenReturn(of(DisplayUser.from(user)));
  }

  private void mockUserWithConfiguration(User user, Locale locale, Topic... excludedTopics) {
    mockUser(user);

    UserMailConfiguration userMailConfiguration = new UserMailConfiguration();
    userMailConfiguration.setLanguage(locale.getLanguage());
    userMailConfiguration.setSummarizeMails(true);
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

  private void mockContentRenderer(Locale locale, String template, Object model, String result) {
    mockContentRenderer(locale, template, model, MailTemplateType.TEXT, MailContent.text(result));
  }

  private void mockContentRenderer(Locale locale, String template, Object model, MailTemplateType type, MailContent content) {
    lenient().when(mailContentRendererFactory.createMailContentRenderer(template, type)).thenReturn(mailContentRenderer);
    lenient().when(mailContentRenderer.createMailContent(locale, model)).thenReturn(content);
  }

  private void configureMailer() {
    when(configuration.isValid()).thenReturn(Boolean.TRUE);
    when(mailer.validate(any(Email.class))).thenReturn(Boolean.TRUE);
  }

  public class TestingMailService extends DefaultMailService {

    private TestingMailService() {
      super(context, userDisplayManager, mailContentRendererFactory, new TestingMailSender(), summaryQueueStore, scheduler);
    }

  }

  public class TestingMailSender extends MailSender {
    public TestingMailSender() {
      super(tracer, sslContextProvider);
    }

    @Override
    Mailer createMailer(MailConfiguration configuration) {
      return mailer;
    }
  }

}
