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
    String path = resolvePath(templatePath, username);

    TemplateEngine templateEngine = templateEngineFactory.getEngineByExtension(templatePath);

    LOG.trace("trying to load template path {} for path {}", path, templatePath);
    Template template = templateEngine.getTemplate(path);
    return getMailContent(template, templateModel);
  }

  private String getMailContent(Template template, Object templateModel) throws IOException {
    StringWriter writer = new StringWriter();
    template.execute(writer, templateModel);
    return writer.toString();
  }

  private String resolvePath(String templatePath, String username) {
    if (configuration.getLanguage() == null) {
      return templatePath;
    }
    String language = userLanguageConfiguration.getUserLanguage(username).orElse(configuration.getLanguage());
    LOG.trace("language for user {}: {}", username, language);
    int lastIndexOfDot = templatePath.lastIndexOf('.');
    String filename = templatePath.substring(0, lastIndexOfDot);
    String extension = templatePath.substring(lastIndexOfDot);
    String templatePathWithLanguage = filename + "_" + language + extension;
    LOG.trace("template path with language for template {}: {}", templatePath, templatePathWithLanguage);
    return templatePathWithLanguage;
  }


}
