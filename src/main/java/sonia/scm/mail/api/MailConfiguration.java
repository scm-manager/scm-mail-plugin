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
