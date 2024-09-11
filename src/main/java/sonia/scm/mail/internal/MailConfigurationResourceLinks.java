/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

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

  public String userConfigLink() {
    return linkBuilder
      .method("getUserConfiguration").parameters()
      .href();
  }

  public String updateUserConfigLink() {
    return linkBuilder
      .method("storeUserConfiguration").parameters()
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

  public String topics() {
    return linkBuilder
      .method("getTopics").parameters()
      .href();
  }
}

