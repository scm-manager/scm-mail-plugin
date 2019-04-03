package sonia.scm.mail.spi;

import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.mail.api.MailConfiguration;
import sonia.scm.mail.api.MailContentRenderer;
import sonia.scm.mail.api.UserLanguageConfiguration;
import sonia.scm.template.Template;
import sonia.scm.template.TemplateEngine;
import sonia.scm.template.TemplateEngineFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Locale;

import static java.util.Locale.ENGLISH;
import static java.util.Optional.ofNullable;

public class DefaultMailContentRenderer implements MailContentRenderer {

  private static final Logger LOG = LoggerFactory.getLogger(DefaultMailContentRenderer.class);

  private final TemplateEngineFactory templateEngineFactory;
  private final String templatePath;
  private final Object templateModel;
  private final MailConfiguration configuration;
  private final UserLanguageConfiguration userLanguageConfiguration;

  @Inject
  public DefaultMailContentRenderer(
    TemplateEngineFactory templateEngineFactory,
    String templatePath,
    Object templateModel,
    MailConfiguration configuration,
    UserLanguageConfiguration userLanguageConfiguration) {
    this.templateEngineFactory = templateEngineFactory;
    this.templatePath = templatePath;
    this.templateModel = templateModel;
    this.configuration = configuration;
    this.userLanguageConfiguration = userLanguageConfiguration;
  }

  @Override
  public String createMailContent(String username) throws Exception {
    Locale preferredLocale = getPreferredLocale(username);

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

  private Locale getPreferredLocale(String username) {
    Locale fallbackLocale = ofNullable(configuration.getLanguage()).map(Locale::new).orElse(ENGLISH);
    return userLanguageConfiguration.getUserLanguage(username).map(Locale::new).orElse(fallbackLocale);
  }
}
