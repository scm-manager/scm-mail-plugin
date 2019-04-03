package sonia.scm.mail.spi;

import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.mail.api.MailConfiguration;
import sonia.scm.mail.api.MailContentRenderer;
import sonia.scm.mail.api.MailContext;
import sonia.scm.mail.api.UserLanguageConfiguration;
import sonia.scm.template.Template;
import sonia.scm.template.TemplateEngine;
import sonia.scm.template.TemplateEngineFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Locale;

import static java.util.Locale.ENGLISH;
import static java.util.Optional.ofNullable;

public class MailContentRendererFactory {

  private static final Logger LOG = LoggerFactory.getLogger(MailContentRendererFactory.class);

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
