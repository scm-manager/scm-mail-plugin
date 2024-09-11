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

package sonia.scm.mail.spi.content;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class HtmlMailAttributeProviderTest {

  private final HtmlMailAttributeProvider attributeProvider = new HtmlMailAttributeProvider();

  private Map<String, String> attributes;

  @BeforeEach
  void setUp() {
    attributes = new HashMap<>();
  }

  @Test
  void shouldAppendStyleAttributeForKnownTag() {
    attributeProvider.setAttributes(null, "h1", attributes);
    assertThat(attributes.get("style")).contains("font-family");
  }

  @Test
  void shouldNotAppendStyleAttributeForUnknownTag() {
    attributeProvider.setAttributes(null, "blink", attributes);
    assertThat(attributes.get("style")).isNull();
  }

}
