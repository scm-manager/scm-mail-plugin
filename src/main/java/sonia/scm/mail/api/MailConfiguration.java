/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * <p>
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 * <p>
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
 * <p>
 * http://bitbucket.org/sdorra/scm-manager
 */


package sonia.scm.mail.api;

import lombok.Setter;
import org.codemonkey.simplejavamail.TransportStrategy;
import sonia.scm.Validateable;
import sonia.scm.mail.internal.XmlCipherStringAdapter;
import sonia.scm.util.Util;
import sonia.scm.util.ValidationUtil;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * Configuration for the {@link MailService}.
 *
 * @author Sebastian Sdorra
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "mail-configuration")
@Setter
public class MailConfiguration implements Validateable {

  /** default from address */
  private String from;

  /** hostname of the smtp server */
  private String host;

  /** password for smtp authentication */
  @XmlJavaTypeAdapter(XmlCipherStringAdapter.class)
  private String password;

  /** port of the smtp server */
  private int port = 25;

  /** prefix for the mail subject */
  @XmlElement(name = "subject-prefix")
  private String subjectPrefix = "[SCM] ";

  /** transoport strategy for the smtp connection */
  @XmlElement(name = "transport-strategy")
  private TransportStrategy transportStrategy = TransportStrategy.SMTP_PLAIN;

  /** username for smtp authentication */
  private String username;

  /**
   * the language used in the mail content
   */
  private String language;

  /**
   * Constructs a new MailConfiguration.
   * This constructor should only be use from JAXB.
   *
   */
  public MailConfiguration() {}


  /**
   * Constructs a new MailConfiguration.
   *
   *
   * @param host hostname of the smtp server
   * @param port port of the smtp server
   * @param transportStrategy transoport strategy for the smtp connection
   * @param from default from address
   * @param subjectPrefix prefix for the mail subject
   */
  public MailConfiguration(String host, int port,
    TransportStrategy transportStrategy, String from, String subjectPrefix)
  {
    this(host, port, transportStrategy, from, null, null, subjectPrefix);
  }


  /**
   * Constructs a new MailConfiguration.
   *
   *
   * @param host hostname of the smtp server
   * @param port port of the smtp server
   * @param transportStrategy transoport strategy for the smtp connection
   * @param from default from address
   * @param username username for smtp authentication
   * @param password password for smtp authentication
   * @param subjectPrefix prefix for the mail subject
   */
  public MailConfiguration(String host, int port,
    TransportStrategy transportStrategy, String from, String username,
    String password, String subjectPrefix)
  {
    this.host = host;
    this.port = port;
    this.transportStrategy = transportStrategy;
    this.from = from;
    this.username = username;
    this.password = password;
    this.subjectPrefix = subjectPrefix;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Returns the default from address.
   *
   *
   * @return default from address
   */
  public String getFrom()
  {
    return from;
  }

  /**
   * Returns hostname of the smtp server.
   *
   *
   * @return hostname of the smtp server
   */
  public String getHost()
  {
    return host;
  }

  /**
   * Returns password for smtp authentication.
   *
   *
   * @return password for smtp authentication
   */
  public String getPassword()
  {
    return password;
  }

  /**
   * Returns port of the smtp server.
   *
   *
   * @return port of the smtp server
   */
  public int getPort()
  {
    return port;
  }

  /**
   * Prefix for the mail subject.
   *
   *
   * @return prefix for the mail subject
   */
  public String getSubjectPrefix()
  {
    return subjectPrefix;
  }

  /**
   * Returns the transoport strategy for the smtp connection.
   *
   *
   * @return transoport strategy for the smtp connection
   */
  public TransportStrategy getTransportStrategy()
  {
    return transportStrategy;
  }

  /**
   * Returns the username for smtp authentication.
   *
   *
   * @return username for smtp authentication
   */
  public String getUsername()
  {
    return username;
  }

  /**
   * Returns true if authentication for the smtp connection is enabled.
   *
   *
   * @return true if authentication for the smtp connection is enabled
   */
  public boolean isAuthenticationEnabled()
  {
    return Util.isNotEmpty(username) && Util.isNotEmpty(password);
  }

  /**
   * Returns true if the configuration is valid.
   *
   *
   * @return true if the configuration is valid
   */
  @Override
  public boolean isValid()
  {
    return Util.isNotEmpty(host) && Util.isNotEmpty(from) && (port > 0) && ValidationUtil.isMailAddressValid(from);
  }

  public String getLanguage() {
    return language;
  }
}
