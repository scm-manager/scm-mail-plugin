package sonia.scm.mail.spi.content;

public class MailContent {

  private final String text;
  private final String html;

  private MailContent(String text, String html) {
    this.text = text;
    this.html = html;
  }

  public String getHtml() {
    return html;
  }

  public String getText() {
    return text;
  }

  public static MailContent html(String content) {
    return new MailContent(null, content);
  }

  public static MailContent text(String content) {
    return new MailContent(content, null);
  }

  public static MailContent textAndHtml(String text, String html) {
    return new MailContent(text, html);
  }
}
