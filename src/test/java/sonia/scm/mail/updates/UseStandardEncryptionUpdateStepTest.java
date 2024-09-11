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

import com.google.common.io.Resources;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.mail.api.MailConfiguration;
import sonia.scm.mail.api.ScmTransportStrategy;
import sonia.scm.mail.updates.UseStandardEncryptionUpdateStep.LegacyMailConfiguration;
import sonia.scm.security.CipherUtil;
import sonia.scm.store.ConfigurationStore;
import sonia.scm.store.ConfigurationStoreFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UseStandardEncryptionUpdateStepTest {

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private ConfigurationStoreFactory storeFactory;
  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private ConfigurationStore<LegacyMailConfiguration> legacyStore;
  @Mock
  private ConfigurationStore<MailConfiguration> newStore;

  @InjectMocks
  private UseStandardEncryptionUpdateStep updateStep;

  @BeforeEach
  void mockStores() {
    when(storeFactory.withType(LegacyMailConfiguration.class).withName("mail").build())
      .thenReturn(legacyStore);
    when(storeFactory.withType(MailConfiguration.class).withName("mail").build())
      .thenReturn(newStore);
  }

  @Test
  void shouldDoNothingWhenNoLegacyConfigurationExists() {
    when(legacyStore.getOptional()).thenReturn(Optional.empty());

    updateStep.doUpdate();

    verify(newStore, never()).set(any());
  }

  @Test
  void shouldConvertPasswordForLegacyConfiguration() throws JAXBException, IOException {
    JAXBContext legacyContext = JAXBContext.newInstance(LegacyMailConfiguration.class);

    String oldXml =
      Resources.toString(
          Resources.getResource("sonia/scm/mail/updates/legacyStore.xml"), Charset.defaultCharset()
        )
        .replace("ENCRYPTED_PASSWORD", CipherUtil.getInstance().encode("marvins_password"));
    LegacyMailConfiguration legacyConfiguration = (LegacyMailConfiguration) legacyContext.createUnmarshaller().unmarshal(new ByteArrayInputStream(oldXml.getBytes(Charset.defaultCharset())));
    when(legacyStore.getOptional()).thenReturn(Optional.of(legacyConfiguration));

    updateStep.doUpdate();

    verify(newStore).set(argThat(
      updatedConfiguration -> {
        assertThat(updatedConfiguration.getPassword()).isEqualTo("marvins_password");
        return true;
      }
    ));
  }

  @ParameterizedTest
  @CsvSource(value = {"SMTP_PLAIN,SMTP", "SMTP_SSL,SMTPS", "SMTP_TLS,SMTP_TLS"})
  void shouldConvertLegacyTransportStrategy(String legacyTransportStrategy, String currentTransportStrategy) throws IOException, JAXBException {
    JAXBContext legacyContext = JAXBContext.newInstance(LegacyMailConfiguration.class);

    String oldXml =
      Resources.toString(
        Resources.getResource("sonia/scm/mail/updates/legacyStore.xml"), Charset.defaultCharset()
      ).replace("SMTP_PLAIN", legacyTransportStrategy);

    LegacyMailConfiguration legacyConfiguration = (LegacyMailConfiguration) legacyContext.createUnmarshaller().unmarshal(new ByteArrayInputStream(oldXml.getBytes(Charset.defaultCharset())));
    when(legacyStore.getOptional()).thenReturn(Optional.of(legacyConfiguration));

    updateStep.doUpdate();

    verify(newStore).set(argThat(
      updatedConfiguration -> {
        assertThat(updatedConfiguration.getTransportStrategy()).isEqualTo(ScmTransportStrategy.valueOf(currentTransportStrategy));
        return true;
      }
    ));
  }
}
