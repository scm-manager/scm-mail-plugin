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

package sonia.scm.mail.updates;

import jakarta.inject.Inject;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import sonia.scm.mail.api.MailConfiguration;
import sonia.scm.mail.api.ScmTransportStrategy;
import sonia.scm.migration.UpdateStep;
import sonia.scm.plugin.Extension;
import sonia.scm.security.CipherUtil;
import sonia.scm.store.ConfigurationStoreFactory;
import sonia.scm.version.Version;

@Extension
public class UseStandardEncryptionUpdateStep implements UpdateStep {

  private final ConfigurationStoreFactory storeFactory;

  @Inject
  public UseStandardEncryptionUpdateStep(ConfigurationStoreFactory storeFactory) {
    this.storeFactory = storeFactory;
  }

  @Override
  public void doUpdate() {
    storeFactory.withType(LegacyMailConfiguration.class).withName("mail").build().getOptional().ifPresent(
      legacyMailConfiguration -> {
        MailConfiguration newMailConfiguration = new MailConfiguration(
          legacyMailConfiguration.host,
          legacyMailConfiguration.port,
          mapTransportStrategy(legacyMailConfiguration.transportStrategy),
          legacyMailConfiguration.from,
          legacyMailConfiguration.username,
          legacyMailConfiguration.password,
          legacyMailConfiguration.subjectPrefix
        );
        newMailConfiguration.setLanguage(legacyMailConfiguration.language);

        storeFactory.withType(MailConfiguration.class).withName("mail").build().set(newMailConfiguration);
      }
    );
  }

  private ScmTransportStrategy mapTransportStrategy(LegacyTransportStrategy legacyTransportStrategy) {
    return switch (legacyTransportStrategy) {
      case SMTP_SSL -> ScmTransportStrategy.SMTPS;
      case SMTP_TLS -> ScmTransportStrategy.SMTP_TLS;
      case SMTP_PLAIN -> ScmTransportStrategy.SMTP;
    };
  }

  @Override
  public Version getTargetVersion() {
    return Version.parse("3.0.0");
  }

  @Override
  public String getAffectedDataType() {
    return "sonia.scm.mail.configuration";
  }

  @XmlAccessorType(XmlAccessType.FIELD)
  @XmlRootElement(name = "mail-configuration")
  @SuppressWarnings("unused")
  static class LegacyMailConfiguration {
    private String from;
    private String host;
    @XmlJavaTypeAdapter(XmlCipherStringAdapter.class)
    private String password;
    private int port;
    @XmlElement(name = "subject-prefix")
    private String subjectPrefix;
    @XmlElement(name = "transport-strategy")
    private LegacyTransportStrategy transportStrategy;
    private String username;
    private String language;
  }

  enum LegacyTransportStrategy {
    SMTP_PLAIN, SMTP_SSL, SMTP_TLS,
  }
}

class XmlCipherStringAdapter extends XmlAdapter<String, String> {

  private static final String PREFIX = "{enc}";

  @Override
  public String marshal(String v) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String unmarshal(String v) {
    if (v.startsWith(PREFIX)) {
      return CipherUtil.getInstance().decode(v.substring(PREFIX.length()));
    }

    return v;
  }
}
