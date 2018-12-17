package sonia.scm.mail.internal;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.sdorra.shiro.ShiroRule;
import com.github.sdorra.shiro.SubjectAware;
import com.google.common.io.Resources;
import com.google.inject.util.Providers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import sonia.scm.api.v2.resources.ScmPathInfoStore;
import sonia.scm.web.JsonEnricherContext;
import sonia.scm.web.VndMediaType;

import javax.inject.Provider;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.URI;
import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MailConfigurationLinkEnricherTest {

  private final ObjectMapper objectMapper = new ObjectMapper();

  private Provider<ScmPathInfoStore> scmPathInfoStoreProvider;

  @Rule
  public ShiroRule shiro = new ShiroRule();

  @Mock
  private JsonEnricherContext context;

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
  public void testEnrich() throws IOException {
    MailConfigurationLinkEnricher enricher = new MailConfigurationLinkEnricher(scmPathInfoStoreProvider, objectMapper);

    URL indexResource = Resources.getResource("sonia/scm/mail/internal/index-001.json");
    JsonNode rootNode = objectMapper.readTree(indexResource);
    when(context.getResponseEntity()).thenReturn(rootNode);
    when(context.getResponseMediaType()).thenReturn(MediaType.valueOf(VndMediaType.INDEX));

    enricher.enrich(context);

    JsonNode mailConfigNode = rootNode.get("_links").get("mailConfig");
    assertThat(mailConfigNode).isNotNull();
    assertThat(mailConfigNode.get("href").asText()).isEqualTo("https://scm-manager.org/scm/api/v2/plugins/mail/config");
  }

  @Test
  @SubjectAware(username = "unpriv",
    password = "secret",
    configuration = "classpath:sonia/scm/mail/internal/shiro.ini"
  )
  public void dontEnrichWhenReadIsNotPermitted() throws IOException {
    MailConfigurationLinkEnricher enricher = new MailConfigurationLinkEnricher(scmPathInfoStoreProvider, objectMapper);

    URL indexResource = Resources.getResource("sonia/scm/mail/internal/index-001.json");
    JsonNode rootNode = objectMapper.readTree(indexResource);
    when(context.getResponseMediaType()).thenReturn(MediaType.valueOf(VndMediaType.INDEX));

    enricher.enrich(context);

    JsonNode mailConfigNode = rootNode.get("_links").get("mailConfig");
    assertThat(mailConfigNode).isNull();
  }

  @Test
  public void testDoNotEnrichNonIndexJson() {
    MailConfigurationLinkEnricher enricher = new MailConfigurationLinkEnricher(scmPathInfoStoreProvider, objectMapper);
    when(context.getResponseMediaType()).thenReturn(MediaType.APPLICATION_JSON_TYPE);

    enricher.enrich(context);

    verify(context, never()).getResponseEntity();
  }


}
