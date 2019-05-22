package sonia.scm.mail.api;

/**
 * The template type defines the rendering method and the content type of the mail.
 */
public enum MailTemplateType {
  /**
   * Renders the given template with a template engine and passes the output to a markdown parser.
   * The final email will contain an html body with predefined styles and a text body.
   */
  MARKDOWN_HTML,

  /**
   * Renders the given template with a template engine and sends the output as html body.
   */
  HTML,

  /**
   * Renders the given template with a template engine and passes the output to a markdown parser.
   * The final email will be an plain text mail.
   */
  MARKDOWN_TEXT,

  /**
   * Renders the given template with a template engine and sends the output as plain text body.
   */
  TEXT
}
