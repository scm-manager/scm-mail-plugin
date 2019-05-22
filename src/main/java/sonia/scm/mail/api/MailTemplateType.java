package sonia.scm.mail.api;

/**
 * The template type defines the rendering method and the content type of the mail.
 */
public enum MailTemplateType {
  /**
   * Renders the given template with a template engine and passes the output to a markdown parser.
   * The final email will be an html mail with predefined styles.
   */
  MARKDOWN_HTML(true, true),

  /**
   * Renders the given template with a template engine and sends the output as html email
   */
  HTML(false, true),

  /**
   * Renders the given template with a template engine and passes the output to a markdown parser.
   * The final email will be an plain text mail.
   */
  MARKDOWN_TEXT(false, true),

  /**
   * Renders the given template with a template engine and sends the output as plain text email.
   */
  TEXT(false, false);

  private boolean html;
  private boolean markdown;

  MailTemplateType(boolean html, boolean markdown) {
    this.html = html;
    this.markdown = markdown;
  }

  /**
   * Returns {@code true} if the email is send as html email.
   *
   * @return {@code true} for html email output
   */
  public boolean isHtml() {
    return html;
  }

  /**
   * Returns {@code true} if the input is markdown text.
   *
   * @return {@code true} for markdown
   */
  public boolean isMarkdown() {
    return markdown;
  }
}
