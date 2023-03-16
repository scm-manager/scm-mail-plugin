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

package sonia.scm.mail.updates;

import com.google.common.base.Strings;
import org.codemonkey.simplejavamail.TransportStrategy;
import sonia.scm.mail.api.MailConfiguration;
import sonia.scm.migration.UpdateStep;
import sonia.scm.plugin.Extension;
import sonia.scm.security.CipherUtil;
import sonia.scm.store.ConfigurationStoreFactory;
import sonia.scm.version.Version;

import javax.inject.Inject;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

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
          legacyMailConfiguration.transportStrategy,
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

  @Override
  public Version getTargetVersion() {
    return Version.parse("2.0.0");
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
    private TransportStrategy transportStrategy;
    private String username;
    private String language;
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
