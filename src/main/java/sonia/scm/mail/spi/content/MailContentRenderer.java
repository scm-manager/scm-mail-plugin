package sonia.scm.mail.spi.content;

import java.util.Locale;

public class MailContentRenderer {

  private final ContentRenderer contentRenderer;
  private final String templatePath;

  MailContentRenderer(ContentRenderer contentRenderer, String templatePath) {
    this.contentRenderer = contentRenderer;
    this.templatePath = templatePath;
  }

  public MailContent createMailContent(Locale preferredLocale, Object templateModel) {
    return contentRenderer.render(new ContentRenderer.Context(templatePath, preferredLocale, templateModel));
  }

}
