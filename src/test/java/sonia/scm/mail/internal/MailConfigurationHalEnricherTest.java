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

  private Provider<ScmPathInfoStore> scmPathInfoStoreProvider;

  @Rule
  public ShiroRule shiro = new ShiroRule();

  @Mock
  private HalAppender appender;

  private MailConfigurationHalEnricher enricher;

  @Before
  public void setUp() {
    ScmPathInfoStore scmPathInfoStore = new ScmPathInfoStore();
    scmPathInfoStore.set(() -> URI.create("https://scm-manager.org/scm/api/"));
    scmPathInfoStoreProvider = Providers.of(scmPathInfoStore);
  }

  @SubjectAware(
    username = "trillian",
    password = "secret",
    configuration = "classpath:sonia/scm/mail/internal/shiro.ini"
  )
  @Test
  public void testEnrich() {
    enricher = new MailConfigurationHalEnricher(scmPathInfoStoreProvider);
    enricher.enrich(HalEnricherContext.of(), appender);
    verify(appender).appendLink("mailConfig", "https://scm-manager.org/scm/api/v2/plugins/mail/config");
  }

  @Test
  @SubjectAware(username = "unpriv",
    password = "secret",
    configuration = "classpath:sonia/scm/mail/internal/shiro.ini"
  )
  public void dontEnrichWhenReadIsNotPermitted() {
    MailConfigurationHalEnricher enricher = new MailConfigurationHalEnricher(scmPathInfoStoreProvider);
    enricher.enrich(HalEnricherContext.of(), appender);
    verifyZeroInteractions(appender);
  }

}
