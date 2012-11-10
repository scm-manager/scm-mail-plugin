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

import sonia.scm.store.Store;
import sonia.scm.store.StoreFactory;
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
  private static final String STORE_NAME = "mail";

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
  public MailContext(StoreFactory storeFactory)
  {
    this.store = storeFactory.getStore(MailConfiguration.class, STORE_NAME);
    this.configuration = this.store.get();
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

    this.store.set(configuration);
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

  //~--- fields ---------------------------------------------------------------

  /** default mail configuration */
  private MailConfiguration configuration;

  /** store for the default mail configuration */
  private Store<MailConfiguration> store;
}
