package sonia.scm.mail.spi;

class MailContent {

  private final String text;
  private final String html;

  private MailContent(String text, String html) {
    this.text = text;
    this.html = html;
  }

  String getHtml() {
    return html;
  }

  String getText() {
    return text;
  }

  static MailContent html(String content) {
    return new MailContent(null, content);
  }

  static MailContent text(String content) {
    return new MailContent(content, null);
  }

  static MailContent textAndHtml(String text, String html) {
    return new MailContent(text, html);
  }
}
