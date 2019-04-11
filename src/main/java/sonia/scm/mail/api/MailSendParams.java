package sonia.scm.mail.api;

import org.codemonkey.simplejavamail.Email;
import sonia.scm.mail.spi.MailContentRenderer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class MailSendParams {

  private final MailConfiguration mailConfiguration;
  private MailContentRenderer mailContentRenderer;
  private Collection<UserEmail> userEmails = new ArrayList<>();

  MailSendParams(MailConfiguration mailConfiguration) {
    this.mailConfiguration = mailConfiguration;
  }

  MailSendParams render(MailContentRenderer mailContentRenderer) {
    this.mailContentRenderer = mailContentRenderer;
    return this;
  }

  public UserEmail forUserId(String userId) {
    return new UserEmail(userId);
  }

  public MailConfiguration getMailConfiguration() {
    return mailConfiguration;
  }

  public MailContentRenderer getMailContentRenderer() {
    return mailContentRenderer;
  }

  public Collection<UserEmail> getUserEmails() {
    return Collections.unmodifiableCollection(userEmails);
  }

  public class UserEmail {
    private final String userId;
    private Email email;

    public UserEmail(String userId) {
      this.userId = userId;
    }

    public MailSendParams sendEmail(Email email) {
      this.email = email;
      MailSendParams.this.userEmails.add(this);
      return MailSendParams.this;
    }

    public String getUserId() {
      return userId;
    }

    public Email getEmail() {
      return email;
    }
  }
}
