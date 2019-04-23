package sonia.scm.mail.spi;

import sonia.scm.template.TemplateEngineFactory;

import javax.inject.Inject;

public class MailContentRendererFactory {

  private final TemplateEngineFactory templateEngineFactory;

  @Inject
  MailContentRendererFactory(TemplateEngineFactory templateEngineFactory) {
    this.templateEngineFactory = templateEngineFactory;
  }

  public MailContentRenderer createMailContentRenderer(String templatePath) {
    return new MailContentRenderer(templateEngineFactory, templatePath);
  }
}
