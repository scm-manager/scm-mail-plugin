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
import sonia.scm.api.v2.resources.LinkAppender;
import sonia.scm.api.v2.resources.LinkEnricherContext;
import sonia.scm.api.v2.resources.ScmPathInfoStore;

import javax.inject.Provider;
import java.net.URI;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

@RunWith(MockitoJUnitRunner.class)
public class MailConfigurationLinkEnricherTest {

  private Provider<ScmPathInfoStore> scmPathInfoStoreProvider;

  @Rule
  public ShiroRule shiro = new ShiroRule();

  @Mock
  private LinkAppender appender;

  private MailConfigurationLinkEnricher enricher;

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
    enricher = new MailConfigurationLinkEnricher(scmPathInfoStoreProvider);
    enricher.enrich(LinkEnricherContext.of(), appender);
    verify(appender).appendOne("mailConfig", "https://scm-manager.org/scm/api/v2/plugins/mail/config");
  }

  @Test
  @SubjectAware(username = "unpriv",
    password = "secret",
    configuration = "classpath:sonia/scm/mail/internal/shiro.ini"
  )
  public void dontEnrichWhenReadIsNotPermitted() {
    MailConfigurationLinkEnricher enricher = new MailConfigurationLinkEnricher(scmPathInfoStoreProvider);
    enricher.enrich(LinkEnricherContext.of(), appender);
    verifyZeroInteractions(appender);
  }

}
