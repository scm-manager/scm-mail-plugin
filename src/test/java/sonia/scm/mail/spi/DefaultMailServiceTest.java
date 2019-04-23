package sonia.scm.mail.spi;

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
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import sonia.scm.mail.api.MailConfiguration;
import sonia.scm.mail.api.MailContext;
import sonia.scm.mail.api.MailSendBatchException;
import sonia.scm.mail.api.MailService;
import sonia.scm.mail.api.UserMailConfiguration;
import sonia.scm.user.DisplayUser;
import sonia.scm.user.User;
import sonia.scm.user.UserDisplayManager;
import sonia.scm.user.UserTestData;

import java.io.IOException;
import java.util.Locale;
import java.util.Optional;

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
  private Mailer mailer;

  private MailService mailService;


  @Captor
  private ArgumentCaptor<Email> emailCaptor;

  @BeforeEach
  void setUpMocks() {
    this.mailService = new TestingMailService();
    when(context.getConfiguration()).thenReturn(configuration);
  }

  @Test
  void shouldSendEmail() throws MailSendBatchException, IOException {
    configureMailer();
    mockContentRenderer(Locale.ENGLISH, "my-template", "model", "Don't Panic");

    mailService.emailTemplateBuilder()
      .toAddress(Locale.ENGLISH, "Tricia McMillan", "tricia.mcmillan@hitchhiker.com")
      .withSubject("Hello World")
      .withTemplate("my-template")
      .andModel("model")
      .send();

    Email email = captureAndReturn();

    assertRecipient(email, "Tricia McMillan", "tricia.mcmillan@hitchhiker.com");

    assertThat(email.getSubject()).isEqualTo("Hello World");
    assertThat(email.getTextHTML()).isEqualTo("Don't Panic");
  }

  private Email captureAndReturn() {
    verify(mailer).sendMail(emailCaptor.capture());

    return emailCaptor.getValue();
  }

  @Test
  void shouldSendEmailAndResolveUser() throws IOException, MailSendBatchException {
    configureMailer();
    mockUser(UserTestData.createTrillian());
    mockContentRenderer(Locale.ENGLISH, "my-template", "model", "Don't Panic");

    mailService.emailTemplateBuilder()
      .toUser("trillian")
      .withSubject("Hello Tricia")
      .withTemplate("my-template")
      .andModel("model")
      .send();

    Email email = captureAndReturn();

    assertRecipient(email, "Tricia McMillan", "tricia.mcmillan@hitchhiker.com");
    assertThat(email.getSubject()).isEqualTo("Hello Tricia");
    assertThat(email.getTextHTML()).isEqualTo("Don't Panic");
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
      .withTemplate("my-template")
      .andModel("model")
      .send();

    Email email = captureAndReturn();

    assertThat(email.getSubject()).isEqualTo("Hallo Tricia");
    assertThat(email.getTextHTML()).isEqualTo("Keine Panik");
  }

  @Test
  void shouldNotSendEmailForUnknownUsers() throws MailSendBatchException, IOException {
    when(configuration.isValid()).thenReturn(Boolean.TRUE);

    mailService.emailTemplateBuilder()
      .toUser("trillian")
      .withSubject("Hello Tricia")
      .withTemplate("my-template")
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
      .withTemplate("my-template")
      .andModel("model")
      .send();

    Email email = captureAndReturn();

    assertThat(email.getSubject()).isEqualTo("Hallo Tricia");
    assertThat(email.getTextHTML()).isEqualTo("Keine Panik");
  }

  private void mockUser(User user) {
    when(userDisplayManager.get(user.getId())).thenReturn(Optional.of(DisplayUser.from(user)));
  }

  private void mockUserWithConfiguration(User user, Locale locale) {
    mockUser(user);

    UserMailConfiguration userMailConfiguration = new UserMailConfiguration();
    userMailConfiguration.setLanguage(locale.getLanguage());
    when(context.getUserConfiguration(user.getId())).thenReturn(Optional.of(userMailConfiguration));
  }

  private void assertRecipient(Email email, String displayName, String address) {
    Recipient recipient = email.getRecipients().get(0);
    assertThat(recipient.getName()).isEqualTo(displayName);
    assertThat(recipient.getAddress()).isEqualTo(address);
  }

  private void mockContentRenderer(Locale locale, String template, Object model, String result) throws IOException {
    when(mailContentRendererFactory.createMailContentRenderer(template)).thenReturn(mailContentRenderer);
    when(mailContentRenderer.createMailContent(locale, model)).thenReturn(result);
  }

  private void configureMailer() {
    when(configuration.isValid()).thenReturn(Boolean.TRUE);
    when(mailer.validate(any(Email.class))).thenReturn(Boolean.TRUE);
  }

  public class TestingMailService extends DefaultMailService {
    private TestingMailService() {
      super(context, userDisplayManager, mailContentRendererFactory);
    }

    @Override
    Mailer createMailer(MailConfiguration configuration) {
      return mailer;
    }
  }

}
