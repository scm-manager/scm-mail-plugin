/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package sonia.scm.mail.internal;

import de.otto.edison.hal.Links;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import sonia.scm.config.ConfigurationPermissions;
import sonia.scm.mail.api.Category;
import sonia.scm.mail.api.MailConfiguration;
import sonia.scm.mail.api.Topic;
import sonia.scm.mail.api.UserMailConfiguration;

import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static de.otto.edison.hal.Link.link;

@Mapper
public abstract class MailConfigurationMapper {

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

  public TopicCollectionDto map(Collection<Topic> availableTopics) {
    Links.Builder links = Links.linkingTo();
    links.self(mailConfigurationResourceLinks.topics());
    List<TopicDto> collection = availableTopics.stream().map(this::map).collect(Collectors.toList());
    return new TopicCollectionDto(links.build(), collection);
  }
}
