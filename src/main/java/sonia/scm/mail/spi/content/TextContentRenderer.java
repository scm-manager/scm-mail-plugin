package sonia.scm.mail.spi.content;

import sonia.scm.template.TemplateEngineFactory;

import javax.inject.Inject;

class TextContentRenderer extends AbstractTemplateContentRenderer {

  @Inject
  TextContentRenderer(TemplateEngineFactory templateEngineFactory) {
    super(templateEngineFactory);
  }

  @Override
  public MailContent render(Context context) {
    return MailContent.text(renderAsString(context));
  }
}
