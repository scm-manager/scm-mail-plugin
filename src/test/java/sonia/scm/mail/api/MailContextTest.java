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

package sonia.scm.mail.api;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import sonia.scm.event.ScmEventBus;
import sonia.scm.store.ConfigurationEntryStoreFactory;
import sonia.scm.store.ConfigurationStoreFactory;
import sonia.scm.store.InMemoryConfigurationEntryStoreFactory;
import sonia.scm.store.InMemoryConfigurationStoreFactory;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static com.google.common.collect.ImmutableSet.of;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;


class MailContextTest {

  ConfigurationStoreFactory configurationStoreFactory = new InMemoryConfigurationStoreFactory();
  ConfigurationEntryStoreFactory entryStoreFactory = new InMemoryConfigurationEntryStoreFactory();

  @Test
  void shouldStoreUserConfig() {
    String userId = "userId";
    UserMailConfiguration unchangedConfig = new UserMailConfiguration();

    UserMailConfiguration summarizationEnabled = new UserMailConfiguration();
    summarizationEnabled.setSummarizeMails(true);

    UserMailConfiguration summarizationFrequencyChanged = new UserMailConfiguration();
    summarizationEnabled.setSummaryFrequency(SummaryFrequency.MINUTES_15);

    MailContext mailContext = new MailContext(configurationStoreFactory, entryStoreFactory, new HashSet<>());


    try (MockedStatic<ScmEventBus> eventBus = mockStatic(ScmEventBus.class)) {
      eventBus.when(ScmEventBus::getInstance).thenReturn(mock(ScmEventBus.class));
      mailContext.store(userId, unchangedConfig);
      eventBus.verifyNoInteractions();
      mailContext.store(userId, summarizationEnabled);
      mailContext.store(userId, summarizationFrequencyChanged);
      eventBus.verify(ScmEventBus::getInstance, times(2));
    }
  }

  @Test
  void shouldCollectTopicsFromAllProviders() {
    TopicProvider hitchhikersGuideProvider = () -> asList(
      new Topic(new Category("Hitchhiker's Guider"), "releases"),
      new Topic(new Category("Hitchhiker's Guider"), "updates"),
      new Topic(new Category("Hitchhiker's Guider"), "amendments")
    );
    TopicProvider deepThoughtProvider = () -> asList(
      new Topic(new Category("Deep Thought"), "answer"),
      new Topic(new Category("Deep Thought"), "question")
    );
    Set<TopicProvider> topicProviders = of(hitchhikersGuideProvider, deepThoughtProvider);

    MailContext mailContext = new MailContext(configurationStoreFactory, entryStoreFactory, topicProviders);
    Collection<Topic> allTopics = mailContext.availableTopics();

    assertThat(allTopics).hasSize(5);
    assertThat(allTopics).contains(hitchhikersGuideProvider.topics().toArray(new Topic[0]));
    assertThat(allTopics).contains(deepThoughtProvider.topics().toArray(new Topic[0]));
  }
}
