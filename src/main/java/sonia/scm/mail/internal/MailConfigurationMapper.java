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

import de.otto.edison.hal.Links;
import jakarta.ws.rs.core.UriInfo;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import sonia.scm.config.ConfigurationPermissions;
import sonia.scm.mail.api.Category;
import sonia.scm.mail.api.MailConfiguration;
import sonia.scm.mail.api.Topic;
import sonia.scm.mail.api.UserMailConfiguration;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static de.otto.edison.hal.Link.link;

@Mapper
public abstract class MailConfigurationMapper {

  @SuppressWarnings("java:S2068")
  private static final String DUMMY_PASSWORD = "__DUMMY__";

  private MailConfigurationResourceLinks mailConfigurationResourceLinks = new MailConfigurationResourceLinks(() -> URI.create("/"));

  @Mapping(target = "attributes", ignore = true)
  abstract MailConfigurationDto map(MailConfiguration mailConfiguration);

  abstract MailConfiguration map(MailConfigurationDto dto);

  @Mapping(target = "attributes", ignore = true)
  abstract UserMailConfigurationDto map(UserMailConfiguration userConfiguration);

  abstract UserMailConfiguration map(UserMailConfigurationDto dto);

  abstract TopicDto map(Topic topic);

  abstract Topic map(TopicDto dto);

  abstract CategoryDto map(Category category);

  abstract Category map(CategoryDto dto);

  abstract Set<Topic> mapTopicDtoCollection(Set<TopicDto> dtos);

  abstract Set<TopicDto> mapTopicCollection(Set<Topic> topics);

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
    links.single(link("availableTopics", mailConfigurationResourceLinks.topics()));
    dto.add(links.build());
  }

  @AfterMapping
  void maskPassword(@MappingTarget MailConfigurationDto dto) {
    dto.setPassword(DUMMY_PASSWORD);
  }

  @AfterMapping
  void removeDummyPassword(@MappingTarget MailConfiguration configuration) {
    if (DUMMY_PASSWORD.equals(configuration.getPassword())) {
      configuration.setPassword(null);
    }
  }

  public TopicCollectionDto map(Collection<Topic> availableTopics) {
    Links.Builder links = Links.linkingTo();
    links.self(mailConfigurationResourceLinks.topics());
    List<TopicDto> collection = availableTopics.stream().map(this::map).collect(Collectors.toList());
    return new TopicCollectionDto(links.build(), collection);
  }
}
