package sonia.scm.mail.spi.content;

import sonia.scm.mail.api.MailTemplateType;

import javax.inject.Inject;
import java.util.Map;

public class MailContentRendererFactory {

  private final Map<MailTemplateType, ContentRenderer> rendererMap;

  @Inject
  public MailContentRendererFactory(Map<MailTemplateType, ContentRenderer> rendererMap) {
    this.rendererMap = rendererMap;
  }

  public MailContentRenderer createMailContentRenderer(String templatePath, MailTemplateType templateType) {
    ContentRenderer contentRenderer = rendererMap.get(templateType);
    return new MailContentRenderer(contentRenderer, templatePath);
  }
}
