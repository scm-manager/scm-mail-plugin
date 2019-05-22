package sonia.scm.mail.spi.content;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.MapBinder;
import sonia.scm.mail.api.MailTemplateType;

public class MailContentModule extends AbstractModule {
  @Override
  protected void configure() {
    MapBinder<MailTemplateType, ContentRenderer> contentRendererMapBinder =
      MapBinder.newMapBinder(binder(), MailTemplateType.class, ContentRenderer.class);

    contentRendererMapBinder.addBinding(MailTemplateType.TEXT).to(TextContentRenderer.class);
    contentRendererMapBinder.addBinding(MailTemplateType.HTML).to(HtmlContentRenderer.class);
    contentRendererMapBinder.addBinding(MailTemplateType.MARKDOWN_TEXT).to(TextMarkdownContentRenderer.class);
    contentRendererMapBinder.addBinding(MailTemplateType.MARKDOWN_HTML).to(HtmlMarkdownContentRenderer.class);

    bind(MailContentRendererFactory.class);
  }
}
