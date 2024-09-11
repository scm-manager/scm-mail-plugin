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

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.event.ScmEventBus;
import sonia.scm.store.ConfigurationEntryStore;
import sonia.scm.store.ConfigurationEntryStoreFactory;
import sonia.scm.store.ConfigurationStore;
import sonia.scm.store.ConfigurationStoreFactory;
import sonia.scm.util.AssertUtil;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toList;

/**
 * Context for the {@link MailService}. This class stores and load the default
 * {@link MailConfiguration} for the {@link MailService}.
 *
 * @author Sebastian Sdorra
 */
@Singleton
public class MailContext {

  /**
   * name of the store
   */
  private static final String CONFIG_STORE_NAME = "mail";
  private static final String USER_CONFIGURATION_STORE_NAME = "user-mail";

  /**
   * the logger for MailContext
   */
  private static final Logger logger =
    LoggerFactory.getLogger(MailContext.class);


  private MailConfiguration configuration;

  private final ConfigurationStore<MailConfiguration> configurationStore;
  private final ConfigurationEntryStore<UserMailConfiguration> userConfigurationStore;

  private final Collection<Topic> availableTopics;

  @Inject
  public MailContext(ConfigurationStoreFactory storeFactory, ConfigurationEntryStoreFactory entryStoreFactory, Set<TopicProvider> topicProviders) {
    this.configurationStore = storeFactory.withType(MailConfiguration.class).withName(CONFIG_STORE_NAME).build();
    this.userConfigurationStore = entryStoreFactory.withType(UserMailConfiguration.class).withName(USER_CONFIGURATION_STORE_NAME).build();
    this.configuration = this.configurationStore.get();
    this.availableTopics = topicProviders.stream()
      .map(TopicProvider::topics)
      .flatMap(Collection::stream)
      .collect(toList());
  }

  /**
   * Stores the given mail configuration as default one.
   *
   * @param configuration default mail configuration
   */
  public void store(MailConfiguration configuration) {
    AssertUtil.assertIsValid(configuration);
    this.configuration = configuration;
    logger.debug("store new mail configuration");

    this.configurationStore.set(configuration);
  }

  public void store(String userId, UserMailConfiguration userMailConfiguration) {
    UserMailConfiguration previousConfig = this.getUserConfiguration(userId).orElse(new UserMailConfiguration());

    if (isMailSummaryChanged(previousConfig, userMailConfiguration)) {
      ScmEventBus.getInstance().post(new SummarizeMailConfigChangedEvent(
        userId, previousConfig, userMailConfiguration
      ));
    }

    userConfigurationStore.put(userId, userMailConfiguration);
  }

  private boolean isMailSummaryChanged(UserMailConfiguration previous, UserMailConfiguration current) {
    return previous.isSummarizeMails() != current.isSummarizeMails() ||
      previous.getSummaryFrequency() != current.getSummaryFrequency();
  }

  /**
   * Returns the current default mail configuration.
   *
   * @return default mail configuration
   */
  public MailConfiguration getConfiguration() {
    if (configuration == null) {
      configuration = new MailConfiguration();
    }

    return configuration;
  }

  public Optional<UserMailConfiguration> getUserConfiguration(String userId) {
    return Optional.ofNullable(userConfigurationStore.get(userId));
  }

  public Collection<Topic> availableTopics() {
    return availableTopics;
  }
}
