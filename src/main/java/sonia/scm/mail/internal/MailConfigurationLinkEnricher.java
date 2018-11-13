package sonia.scm.mail.internal;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import sonia.scm.api.v2.resources.LinkBuilder;
import sonia.scm.api.v2.resources.ScmPathInfo;
import sonia.scm.api.v2.resources.ScmPathInfoStore;
import sonia.scm.plugin.Extension;
import sonia.scm.web.JsonEnricherBase;
import sonia.scm.web.JsonEnricherContext;
import sonia.scm.web.VndMediaType;

import javax.inject.Inject;
import javax.inject.Provider;

@Extension
public class MailConfigurationLinkEnricher extends JsonEnricherBase {

  private Provider<ScmPathInfoStore> scmPathInfoStoreProvider;

  @Inject
  public MailConfigurationLinkEnricher(Provider<ScmPathInfoStore> scmPathInfoStoreProvider, ObjectMapper objectMapper) {
    super(objectMapper);
    this.scmPathInfoStoreProvider = scmPathInfoStoreProvider;
  }

  @Override
  public void enrich(JsonEnricherContext context) {
    // TODO check read permission
    if (resultHasMediaType(VndMediaType.INDEX, context)) {
      JsonNode rootNode = context.getResponseEntity();

      ObjectNode mailLinkNode = createObject().put("href", createLink());
      addPropertyNode(rootNode.get("_links"), "mailConfig", mailLinkNode);
    }
  }

  private String createLink() {
    return new LinkBuilder(scmPathInfoStoreProvider.get().get(), MailConfigurationResource.class)
      .method("getConfiguration")
      .parameters()
      .href();
  }
}
