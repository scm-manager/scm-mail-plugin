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

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.Setter;
import sonia.scm.Validateable;
import sonia.scm.util.Util;
import sonia.scm.util.ValidationUtil;
import sonia.scm.xml.XmlCipherStringAdapter;


/**
 * Configuration for the {@link MailService}.
 *
 * @author Sebastian Sdorra
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "mail-configuration")
@Setter
public class MailConfiguration implements Validateable {

  /**
   * default from address
   */
  private String from;

  /**
   * True if default from address is always used as sender (instead of user)
   */
  private boolean fromAddressAsSender;

  /**
   * hostname of the smtp server
   */
  private String host;

  /**
   * password for smtp authentication
   */
  @XmlJavaTypeAdapter(XmlCipherStringAdapter.class)
  private String password;

  /**
   * port of the smtp server
   */
  private int port = 25;

  /**
   * prefix for the mail subject
   */
  @XmlElement(name = "subject-prefix")
  private String subjectPrefix = "[SCM] ";

  /**
   * transoport strategy for the smtp connection
   */
  @XmlElement(name = "transport-strategy")
  private ScmTransportStrategy transportStrategy = ScmTransportStrategy.SMTP;

  /**
   * username for smtp authentication
   */
  private String username;

  /**
   * the language used in the mail content
   */
  private String language;

  /**
   * Constructs a new MailConfiguration.
   * This constructor should only be use from JAXB.
   */
  public MailConfiguration() {
  }


  /**
   * Constructs a new MailConfiguration.
   *
   * @param host              hostname of the smtp server
   * @param port              port of the smtp server
   * @param transportStrategy transoport strategy for the smtp connection
   * @param from              default from address
   * @param subjectPrefix     prefix for the mail subject
   */
  public MailConfiguration(String host, int port,
                           ScmTransportStrategy transportStrategy, String from, String subjectPrefix) {
    this(host, port, transportStrategy, from, null, null, subjectPrefix);
  }


  /**
   * Constructs a new MailConfiguration.
   *
   * @param host              hostname of the smtp server
   * @param port              port of the smtp server
   * @param transportStrategy transoport strategy for the smtp connection
   * @param from              default from address
   * @param username          username for smtp authentication
   * @param password          password for smtp authentication
   * @param subjectPrefix     prefix for the mail subject
   */
  public MailConfiguration(String host, int port,
                           ScmTransportStrategy transportStrategy, String from, String username,
                           String password, String subjectPrefix) {
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
   * @return default from address
   */
  public String getFrom() {
    return from;
  }

  /**
   * Returns hostname of the smtp server.
   *
   * @return hostname of the smtp server
   */
  public String getHost() {
    return host;
  }

  /**
   * Returns password for smtp authentication.
   *
   * @return password for smtp authentication
   */
  public String getPassword() {
    return password;
  }

  /**
   * Returns port of the smtp server.
   *
   * @return port of the smtp server
   */
  public int getPort() {
    return port;
  }

  /**
   * Prefix for the mail subject.
   *
   * @return prefix for the mail subject
   */
  public String getSubjectPrefix() {
    return subjectPrefix;
  }

  /**
   * Returns the transoport strategy for the smtp connection.
   *
   * @return transoport strategy for the smtp connection
   */
  public ScmTransportStrategy getTransportStrategy() {
    return transportStrategy;
  }

  /**
   * Returns the username for smtp authentication.
   *
   * @return username for smtp authentication
   */
  public String getUsername() {
    return username;
  }

  public boolean getFromAddressAsSender() {
    return fromAddressAsSender;
  }

  /**
   * Returns true if the configuration is valid.
   *
   * @return true if the configuration is valid
   */
  @Override
  public boolean isValid() {
    return Util.isNotEmpty(host) && Util.isNotEmpty(from) && (port > 0) && ValidationUtil.isMailAddressValid(from);
  }

  public String getLanguage() {
    return language;
  }
}
