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

package sonia.scm.mail.api;

import org.junit.jupiter.api.Test;
import sonia.scm.store.ConfigurationEntryStoreFactory;
import sonia.scm.store.ConfigurationStoreFactory;
import sonia.scm.store.InMemoryConfigurationEntryStoreFactory;
import sonia.scm.store.InMemoryConfigurationStoreFactory;

import java.util.Collection;
import java.util.Set;

import static com.google.common.collect.ImmutableSet.of;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;


class MailContextTest {

  ConfigurationStoreFactory configurationStoreFactory = new InMemoryConfigurationStoreFactory();
  ConfigurationEntryStoreFactory entryStoreFactory = new InMemoryConfigurationEntryStoreFactory();

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
