/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.mail.api;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import sonia.scm.store.ConfigurationStore;
import sonia.scm.store.ConfigurationStoreFactory;

import sonia.scm.util.AssertUtil;

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
  private static final String USER_LANGUAGE_STORE_NAME = "user-lang";

  /**
   * the logger for MailContext
   */
  private static final Logger logger =
    LoggerFactory.getLogger(MailContext.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs a new MailContext.
   *
   *
   * @param storeFactory store factory
   */
  @Inject
  public MailContext(ConfigurationStoreFactory storeFactory) {
    this.configurationStore = storeFactory.withType(MailConfiguration.class).withName(CONFIG_STORE_NAME).build();
    this.userLanguageStore = storeFactory.withType(UserLanguageConfiguration.class).withName(USER_LANGUAGE_STORE_NAME).build();
    this.configuration = this.configurationStore.get();
    this.userLanguageConfiguration = this.userLanguageStore.get();
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
    UserLanguageConfiguration userLanguageConfiguration = getUserLanguageConfiguration();
    userLanguageConfiguration.setUserMailConfiguration(userId, userMailConfiguration);
    store(userLanguageConfiguration);
  }

  private void store(UserLanguageConfiguration userLanguageConfiguration)
  {
    this.userLanguageConfiguration = userLanguageConfiguration;
    logger.debug("store new user language configuration");

    this.userLanguageStore.set(userLanguageConfiguration);
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

  public UserLanguageConfiguration getUserLanguageConfiguration() {
    if (userLanguageConfiguration == null) {
      userLanguageConfiguration = new UserLanguageConfiguration();
    }
    return userLanguageConfiguration;
  }

  public UserMailConfiguration getUserConfiguration(String userId) {
    UserMailConfiguration userMailConfiguration = getUserLanguageConfiguration().getUserMailConfigurations().get(userId);
    if (userMailConfiguration == null){
      userMailConfiguration = new UserMailConfiguration();
    }
    return userMailConfiguration;
  }

  //~--- fields ---------------------------------------------------------------

  /** default mail configuration */
  private MailConfiguration configuration;

  private UserLanguageConfiguration userLanguageConfiguration;

  private final ConfigurationStore<MailConfiguration> configurationStore;
  private final ConfigurationStore<UserLanguageConfiguration> userLanguageStore;
}
