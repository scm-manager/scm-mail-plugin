/**
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
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
