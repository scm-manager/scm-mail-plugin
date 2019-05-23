package sonia.scm.mail.spi.content;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.mail.api.MailTemplateType;
import sonia.scm.template.Template;
import sonia.scm.template.TemplateEngine;
import sonia.scm.template.TemplateEngineFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MailContentModuleTest {

  @Mock
  private TemplateEngineFactory templateEngineFactory;

  @Mock
  private TemplateEngine templateEngine;

  @InjectMocks
  private MockModule mockModule;

  @BeforeEach
  void setUp() {
    when(templateEngineFactory.getEngineByExtension(anyString())).thenReturn(templateEngine);
  }

  @Test
  void shouldInjectMap() {
    Injector injector = Guice.createInjector(mockModule, new MailContentModule());
    MailContentRendererFactory instance = injector.getInstance(MailContentRendererFactory.class);
    assertThat(instance).isNotNull();
  }

  public static class MockModule extends AbstractModule {

    private TemplateEngineFactory templateEngineFactory;

    public MockModule(TemplateEngineFactory templateEngineFactory) {
      this.templateEngineFactory = templateEngineFactory;
    }

    @Override
    protected void configure() {
      bind(TemplateEngineFactory.class).toInstance(templateEngineFactory);
    }
  }

}
