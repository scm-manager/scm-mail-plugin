package sonia.scm.mail.internal;

import sonia.scm.api.v2.resources.Enrich;
import sonia.scm.api.v2.resources.HalAppender;
import sonia.scm.api.v2.resources.HalEnricher;
import sonia.scm.api.v2.resources.HalEnricherContext;
import sonia.scm.api.v2.resources.LinkBuilder;
import sonia.scm.api.v2.resources.Me;
import sonia.scm.api.v2.resources.ScmPathInfoStore;
import sonia.scm.plugin.Extension;

import javax.inject.Inject;
import javax.inject.Provider;

@Extension
@Enrich(Me.class)
public class MeEnricher implements HalEnricher {

  private Provider<ScmPathInfoStore> scmPathInfoStoreProvider;

  @Inject
  public MeEnricher(Provider<ScmPathInfoStore> scmPathInfoStoreProvider) {
    this.scmPathInfoStoreProvider = scmPathInfoStoreProvider;
  }

  @Override
  public void enrich(HalEnricherContext context, HalAppender appender) {
    String getUserConfigurationLink = new LinkBuilder(scmPathInfoStoreProvider.get().get(), MailConfigurationResource.class)
      .method("getUserConfiguration")
      .parameters()
      .href();
    appender.appendLink("mailConfig", getUserConfigurationLink);
  }
}
