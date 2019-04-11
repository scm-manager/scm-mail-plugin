package sonia.scm.mail.spi;

import org.codemonkey.simplejavamail.Email;
import org.codemonkey.simplejavamail.Mailer;
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
import sonia.scm.mail.api.MailContentRenderer;
import sonia.scm.mail.api.MailContext;
import sonia.scm.mail.api.MailSendParams;

import javax.mail.Message;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DefaultMailServiceTest {

  private static final String USER_1 = "user 1";
  private static final String USER_2 = "user 2";

  @Mock
  MailContext context;
  @Mock
  MailConfiguration configuration;
  @Mock
  MailContentRenderer renderer;
  @Mock
  Mailer mailer;

  @Captor
  ArgumentCaptor<Email> sentMailCaptor;
  private DefaultMailService service;

  @BeforeEach
  void init() throws Exception {
    when(context.getConfiguration()).thenReturn(configuration);
    when(context.createMailContentRenderer(any(), any())).thenReturn(renderer);
    when(context.withTemplate(any())).thenCallRealMethod();
    when(configuration.isValid()).thenReturn(true);
    doNothing().when(mailer).sendMail(sentMailCaptor.capture());
    when(mailer.validate(any())).thenReturn(true);
    when(renderer.createMailContent(USER_1)).thenReturn("content user 1");
    when(renderer.createMailContent(USER_2)).thenReturn("content user 2");

    service = new DefaultMailService(context) {
      @Override
      Mailer createMailer(MailConfiguration configuration) {
        return mailer;
      }
    };
  }

  @Test
  void shouldSendMails() throws Exception {
    MailSendParams params = context.withTemplate(null)
      .andModel(null)
      .forUserId(USER_1).sendEmail(to(USER_1))
      .forUserId(USER_2).sendEmail(to(USER_2));
    service.send(params);

    List<Email> sendMails = sentMailCaptor.getAllValues();

    assertThat(sendMails).hasSize(2)
    .anyMatch(email -> email.getTextHTML().contains(USER_1) && email.getRecipients().stream().anyMatch(r -> USER_1.equals(r.getAddress())))
    .anyMatch(email -> email.getTextHTML().contains(USER_2) && email.getRecipients().stream().anyMatch(r -> USER_2.equals(r.getAddress())));
  }

  private Email to(String user) {
    Email email = new Email();
    email.addRecipient(user, user, Message.RecipientType.TO);
    return email;
  }
}
