package sonia.scm.mail.spi;

import com.google.inject.Inject;
import sonia.scm.mail.api.MailContentRenderer;
import sonia.scm.mail.api.MailContext;
import sonia.scm.template.TemplateEngineFactory;

public class MailContentRendererFactory {

  private final TemplateEngineFactory templateEngineFactory;
  private final MailContext mailContext;

  @Inject
  public MailContentRendererFactory(
    TemplateEngineFactory templateEngineFactory,
    MailContext mailContext) {
    this.templateEngineFactory = templateEngineFactory;
    this.mailContext = mailContext;
  }

  public MailContentRenderer createFor(String templatePath, Object templateModel) {
    return new DefaultMailContentRenderer(templateEngineFactory, templatePath, templateModel, mailContext.getConfiguration(), mailContext.getUserLanguageConfiguration());
  }
}
