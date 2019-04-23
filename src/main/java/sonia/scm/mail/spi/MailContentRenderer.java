package sonia.scm.mail.spi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.template.Template;
import sonia.scm.template.TemplateEngine;
import sonia.scm.template.TemplateEngineFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Locale;

public class MailContentRenderer {

  private static final Logger LOG = LoggerFactory.getLogger(MailContentRenderer.class);

  private final TemplateEngineFactory templateEngineFactory;
  private final String templatePath;

  MailContentRenderer(TemplateEngineFactory templateEngineFactory, String templatePath) {
    this.templateEngineFactory = templateEngineFactory;
    this.templatePath = templatePath;
  }

  String createMailContent(Locale preferredLocale, Object templateModel) throws IOException {
    TemplateEngine templateEngine = templateEngineFactory.getEngineByExtension(templatePath);

    LOG.trace("trying to load template path {} with preferred locale {}", templatePath, preferredLocale);
    Template template = templateEngine.getTemplate(templatePath, preferredLocale);
    return getMailContent(template, templateModel);
  }

  private String getMailContent(Template template, Object templateModel) throws IOException {
    StringWriter writer = new StringWriter();
    template.execute(writer, templateModel);
    return writer.toString();
  }
}
