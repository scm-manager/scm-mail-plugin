package sonia.scm.mail.spi.content;

class MailContentException extends RuntimeException {

  MailContentException(String message) {
    super(message);
  }

  MailContentException(String message, Throwable cause) {
    super(message, cause);
  }
}
