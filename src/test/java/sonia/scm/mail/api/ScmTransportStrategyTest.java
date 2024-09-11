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


import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.simplejavamail.api.mailer.config.TransportStrategy;

import static org.assertj.core.api.Assertions.assertThat;

class ScmTransportStrategyTest {

  @ParameterizedTest
  @CsvSource(value = {"SMTP,SMTP", "SMTPS,SMTPS", "SMTP_TLS,SMTP_TLS"})
  void shouldMapCorrectly(String scmEnumName, String dependencyEnumName) {
    ScmTransportStrategy strategy = ScmTransportStrategy.valueOf(scmEnumName);
    assertThat(strategy.getTransportStrategy()).isEqualTo(TransportStrategy.valueOf(dependencyEnumName));
  }
}
