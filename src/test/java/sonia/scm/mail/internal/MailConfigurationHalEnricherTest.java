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
package sonia.scm.mail.internal;

import com.github.sdorra.shiro.ShiroRule;
import com.github.sdorra.shiro.SubjectAware;
import com.google.inject.util.Providers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import sonia.scm.api.v2.resources.HalAppender;
import sonia.scm.api.v2.resources.HalEnricherContext;
import sonia.scm.api.v2.resources.ScmPathInfoStore;

import javax.inject.Provider;
import java.net.URI;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

@RunWith(MockitoJUnitRunner.class)
public class MailConfigurationHalEnricherTest {

  @Rule
  public ShiroRule shiro = new ShiroRule();

  @Mock
  private HalAppender appender;

  private MailConfigurationHalEnricher enricher;

  @Before
  public void setUp() {
    ScmPathInfoStore scmPathInfoStore = new ScmPathInfoStore();
    scmPathInfoStore.set(() -> URI.create("https://scm-manager.org/scm/api/"));
    Provider<ScmPathInfoStore> scmPathInfoStoreProvider = Providers.of(scmPathInfoStore);
    enricher = new MailConfigurationHalEnricher(scmPathInfoStoreProvider);
  }

  @SubjectAware(
    username = "trillian",
    password = "secret",
    configuration = "classpath:sonia/scm/mail/internal/shiro.ini"
  )
  @Test
  public void testEnrich() {
    enricher.enrich(HalEnricherContext.of(), appender);
    verify(appender).appendLink("mailConfig", "https://scm-manager.org/scm/api/v2/plugins/mail/config");
  }

  @Test
  @SubjectAware(username = "unpriv",
    password = "secret",
    configuration = "classpath:sonia/scm/mail/internal/shiro.ini"
  )
  public void dontEnrichWhenReadIsNotPermitted() {
    enricher.enrich(HalEnricherContext.of(), appender);
    verifyZeroInteractions(appender);
  }
}
