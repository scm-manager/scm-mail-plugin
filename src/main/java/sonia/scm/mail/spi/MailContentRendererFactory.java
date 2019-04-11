package sonia.scm.mail.spi;

import sonia.scm.mail.api.MailContext;
import sonia.scm.template.TemplateEngineFactory;

import javax.inject.Inject;

public class MailContentRendererFactory {

  private final TemplateEngineFactory templateEngineFactory;

  @Inject
  MailContentRendererFactory(TemplateEngineFactory templateEngineFactory) {
    this.templateEngineFactory = templateEngineFactory;
  }

  public MailContentRenderer createMailContentRenderer(String templatePath, Object templateModel, MailContext context) {
    return new MailContentRenderer(templateEngineFactory, templatePath, templateModel, context);
  }
}
