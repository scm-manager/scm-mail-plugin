package sonia.scm.mail.internal;

import de.otto.edison.hal.Links;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import sonia.scm.config.ConfigurationPermissions;
import sonia.scm.mail.api.MailConfiguration;
import sonia.scm.mail.api.UserMailConfiguration;

import javax.ws.rs.core.UriInfo;
import java.net.URI;

import static de.otto.edison.hal.Link.link;

@Mapper
public abstract class MailConfigurationMapper {

  private MailConfigurationResourceLinks mailConfigurationResourceLinks = new MailConfigurationResourceLinks(() -> URI.create("/"));

  @Mapping(target = "attributes", ignore = true)
  protected abstract MailConfigurationDto map(MailConfiguration mailConfiguration);

  protected abstract MailConfiguration map(MailConfigurationDto dto);

  @Mapping(target = "attributes", ignore = true)
  protected abstract UserMailConfigurationDto map(UserMailConfiguration userConfiguration);

  protected abstract UserMailConfiguration map(UserMailConfigurationDto dto);

  public MailConfigurationMapper using(UriInfo uriInfo) {
    mailConfigurationResourceLinks = new MailConfigurationResourceLinks(uriInfo::getBaseUri);
    return this;
  }

  @AfterMapping
  void addLinks(@MappingTarget MailConfigurationDto dto) {
    Links.Builder links = Links.linkingTo();
    links.self(mailConfigurationResourceLinks.self());
    if (ConfigurationPermissions.write("mail").isPermitted()) {
      links.single(link("update", mailConfigurationResourceLinks.update()));
      links.single(link("test", mailConfigurationResourceLinks.test()));
    }
    dto.add(links.build());
  }

  @AfterMapping
  void addUserConfigLinks(@MappingTarget UserMailConfigurationDto dto) {
    Links.Builder links = Links.linkingTo();
    links.self(mailConfigurationResourceLinks.userConfigLink());
    links.single(link("update", mailConfigurationResourceLinks.updateUserConfigLink()));
    dto.add(links.build());
  }
}
