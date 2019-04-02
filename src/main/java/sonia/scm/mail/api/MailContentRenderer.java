package sonia.scm.mail.api;

public interface MailContentRenderer {

  String createMailContent(String username) throws Exception;
}
