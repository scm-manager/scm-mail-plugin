package sonia.scm.mail.spi.content;

import java.util.Locale;

interface ContentRenderer {

  MailContent render(Context context);

  class Context {
    private final String templatePath;
    private final Locale preferredLocale;
    private final Object templateModel;

    Context(String templatePath, Locale preferredLocale, Object templateModel) {
      this.templatePath = templatePath;
      this.preferredLocale = preferredLocale;
      this.templateModel = templateModel;
    }

    String getTemplatePath() {
      return templatePath;
    }

    Locale getPreferredLocale() {
      return preferredLocale;
    }

    Object getTemplateModel() {
      return templateModel;
    }
  }

}
