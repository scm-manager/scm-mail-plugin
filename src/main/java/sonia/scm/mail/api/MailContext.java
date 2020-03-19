/**
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

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.store.ConfigurationEntryStore;
import sonia.scm.store.ConfigurationEntryStoreFactory;
import sonia.scm.store.ConfigurationStore;
import sonia.scm.store.ConfigurationStoreFactory;
import sonia.scm.util.AssertUtil;

import java.util.Optional;

/**
 * Context for the {@link MailService}. This class stores and load the default
 * {@link MailConfiguration} for the {@link MailService}.
 *
 * @author Sebastian Sdorra
 */
@Singleton
public class MailContext
{

  /** name of the store */
  private static final String CONFIG_STORE_NAME = "mail";
  private static final String USER_CONFIGURATION_STORE_NAME = "user-mail";

  /**
   * the logger for MailContext
   */
  private static final Logger logger =
    LoggerFactory.getLogger(MailContext.class);

  //~--- constructors ---------------------------------------------------------

  @Inject
  public MailContext(ConfigurationStoreFactory storeFactory, ConfigurationEntryStoreFactory entryStoreFactory) {
    this.configurationStore = storeFactory.withType(MailConfiguration.class).withName(CONFIG_STORE_NAME).build();
    this.userConfigurationStore = entryStoreFactory.withType(UserMailConfiguration.class).withName(USER_CONFIGURATION_STORE_NAME).build();
    this.configuration = this.configurationStore.get();
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Stores the given mail configuration as default one.
   *
   *
   * @param configuration default mail configuration
   */
  public void store(MailConfiguration configuration)
  {
    AssertUtil.assertIsValid(configuration);
    this.configuration = configuration;
    logger.debug("store new mail configuration");

    this.configurationStore.set(configuration);
  }

  public void store(String userId, UserMailConfiguration userMailConfiguration) {
    userConfigurationStore.put(userId, userMailConfiguration);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Returns the current default mail configuration.
   *
   *
   * @return default mail configuration
   */
  public MailConfiguration getConfiguration()
  {
    if (configuration == null)
    {
      configuration = new MailConfiguration();
    }

    return configuration;
  }

  public Optional<UserMailConfiguration> getUserConfiguration(String userId) {
    return Optional.ofNullable(userConfigurationStore.get(userId));
  }

  //~--- fields ---------------------------------------------------------------

  /** default mail configuration */
  private MailConfiguration configuration;

  private final ConfigurationStore<MailConfiguration> configurationStore;
  private final ConfigurationEntryStore<UserMailConfiguration> userConfigurationStore;
}
