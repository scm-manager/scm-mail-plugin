package sonia.scm.mail.api;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.codemonkey.simplejavamail.Email;

@Builder
@Getter
@Setter
public class MailSendParams {

  MailConfiguration mailConfiguration;
  MailContentRenderer mailContentRenderer;
  Iterable<Email> emails;
  String userId;

}
