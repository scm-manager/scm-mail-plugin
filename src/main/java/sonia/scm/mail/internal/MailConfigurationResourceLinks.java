package sonia.scm.mail.internal;

import sonia.scm.api.v2.resources.LinkBuilder;
import sonia.scm.api.v2.resources.ScmPathInfo;

@SuppressWarnings("squid:S1192")
public class MailConfigurationResourceLinks {
  private final LinkBuilder linkBuilder;

  public MailConfigurationResourceLinks(ScmPathInfo scmPathInfo) {
    this.linkBuilder = new LinkBuilder(scmPathInfo, MailConfigurationResource.class);
  }

  public String self() {
    return linkBuilder
      .method("getConfiguration").parameters()
      .href();
  }

  public String update() {
    return linkBuilder
      .method("updateConfiguration").parameters()
      .href();
  }

  public String test() {
    return linkBuilder
      .method("sendTestMessage").parameters()
      .href();
  }
}

