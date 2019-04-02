package sonia.scm.mail.spi;

import com.google.inject.Inject;
import sonia.scm.mail.api.MailConfiguration;
import sonia.scm.mail.api.MailContentRenderer;
import sonia.scm.plugin.PluginLoader;
import sonia.scm.template.Template;
import sonia.scm.template.TemplateEngine;
import sonia.scm.template.TemplateEngineFactory;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.net.URL;

public class DefaultMailContentRenderer implements MailContentRenderer {

  private final TemplateEngineFactory templateEngineFactory;
  private final String templatePath;
  private final Object templateModel;
  private final MailConfiguration configuration;
  private final PluginLoader pluginLoader;

  @Inject
  public DefaultMailContentRenderer(TemplateEngineFactory templateEngineFactory, String templatePath, Object templateModel, MailConfiguration configuration, PluginLoader pluginLoader) {
    this.templateEngineFactory = templateEngineFactory;
    this.templatePath = templatePath;
    this.templateModel = templateModel;
    this.configuration = configuration;
    this.pluginLoader = pluginLoader;
  }

  public String getMailContent(Template template, Object templateModel) throws IOException {
    StringWriter writer = new StringWriter();
    template.execute(writer, templateModel);
    return writer.toString();
  }

  @Override
  public String createMailContent(String username) throws Exception {
    String path = resolvePath(templatePath, username);

    TemplateEngine templateEngine = templateEngineFactory.getEngineByExtension(templatePath);

    ClassLoader uberClassLoader = pluginLoader.getUberClassLoader();
    URL resource = uberClassLoader.getResource(path);
    if (resource == null) {
      throw new Exception("resource of path " + templatePath + " cannot be found");
    }
    File file = new File(resource.getFile());
    Reader reader = new FileReader(file);

    Thread thread = Thread.currentThread();
    ClassLoader contextClassLoader = thread.getContextClassLoader();

    thread.setContextClassLoader(uberClassLoader);
    Template template = templateEngine.getTemplate(file.getAbsolutePath(), reader);
    thread.setContextClassLoader(contextClassLoader);

    return getMailContent(template, templateModel);

  }

  private String resolvePath(String templatePath, String username) {
    if (configuration.getLanguage() == null) {
      return templatePath;
    }
    String language = configuration.getUserLanguage(username);
    int lastIndexOfDot = templatePath.lastIndexOf(".");
    String filename = templatePath.substring(0, lastIndexOfDot);
    String extension = templatePath.substring(lastIndexOfDot);
    return filename + "_" + language + extension;
  }


}
