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



package sonia.scm.mail.config;

//~--- non-JDK imports --------------------------------------------------------

import org.codemonkey.simplejavamail.TransportStrategy;

import sonia.scm.Validateable;
import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 *
 * @author Sebastian Sdorra
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "mail-configuration")
public class MailConfiguration implements Validateable
{

  /**
   * Constructs ...
   *
   */
  public MailConfiguration() {}

  /**
   * Constructs ...
   *
   *
   * @param host
   * @param port
   * @param transportStrategy
   * @param subjectPrefix
   */
  public MailConfiguration(String host, int port,
    TransportStrategy transportStrategy, String subjectPrefix)
  {
    this(host, port, transportStrategy, null, null, subjectPrefix);
  }

  /**
   * Constructs ...
   *
   *
   * @param host
   * @param port
   * @param transportStrategy
   * @param username
   * @param password
   * @param subjectPrefix
   */
  public MailConfiguration(String host, int port,
    TransportStrategy transportStrategy, String username, String password,
    String subjectPrefix)
  {
    this.host = host;
    this.port = port;
    this.transportStrategy = transportStrategy;
    this.username = username;
    this.password = password;
    this.subjectPrefix = subjectPrefix;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public String getFrom()
  {
    return from;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getHost()
  {
    return host;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getPassword()
  {
    return password;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public int getPort()
  {
    return port;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getSubjectPrefix()
  {
    return subjectPrefix;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public TransportStrategy getTransportStrategy()
  {
    return transportStrategy;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getUsername()
  {
    return username;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public boolean isAuthenticationEnabled()
  {
    return Util.isNotEmpty(username) && Util.isNotEmpty(password);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public boolean isValid()
  {
    return Util.isNotEmpty(host) && Util.isNotEmpty(from) && (port > 0);
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private String from;

  /** Field description */
  private String host;

  /** Field description */
  @XmlJavaTypeAdapter(XmlCipherStringAdapter.class)
  private String password;

  /** Field description */
  private int port = 25;

  /** Field description */
  @XmlElement(name = "subject-prefix")
  private String subjectPrefix = "[SCM] ";

  /** Field description */
  @XmlElement(name = "transport-strategy")
  private TransportStrategy transportStrategy = TransportStrategy.SMTP_PLAIN;

  /** Field description */
  private String username;
}
