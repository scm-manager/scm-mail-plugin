package sonia.scm.mail.spi.content;

import sonia.scm.template.TemplateEngineFactory;

import javax.inject.Inject;

class HtmlContentRenderer extends AbstractTemplateContentRenderer {

  @Inject
  HtmlContentRenderer(TemplateEngineFactory templateEngineFactory) {
    super(templateEngineFactory);
  }

  @Override
  public MailContent render(Context context) {
    return MailContent.html(renderAsString(context));
  }
}
