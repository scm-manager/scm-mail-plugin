package sonia.scm.mail.internal;

import sonia.scm.api.v2.resources.Enrich;
import sonia.scm.api.v2.resources.Index;
import sonia.scm.api.v2.resources.LinkAppender;
import sonia.scm.api.v2.resources.LinkBuilder;
import sonia.scm.api.v2.resources.LinkEnricher;
import sonia.scm.api.v2.resources.LinkEnricherContext;
import sonia.scm.api.v2.resources.ScmPathInfoStore;
import sonia.scm.config.ConfigurationPermissions;
import sonia.scm.plugin.Extension;

import javax.inject.Inject;
import javax.inject.Provider;

@Extension
@Enrich(Index.class)
public class MailConfigurationLinkEnricher implements LinkEnricher {

  private Provider<ScmPathInfoStore> scmPathInfoStoreProvider;

  @Inject
  public MailConfigurationLinkEnricher(Provider<ScmPathInfoStore> scmPathInfoStoreProvider) {
    this.scmPathInfoStoreProvider = scmPathInfoStoreProvider;
  }


  private String createLink() {
    return new LinkBuilder(scmPathInfoStoreProvider.get().get(), MailConfigurationResource.class)
      .method("getConfiguration")
      .parameters()
      .href();
  }

  @Override
  public void enrich(LinkEnricherContext context, LinkAppender appender) {
    if (ConfigurationPermissions.read("mail").isPermitted()) {
      appender.appendOne("mailConfig", createLink());
    }
  }
}
