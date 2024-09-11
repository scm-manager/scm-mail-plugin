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

package sonia.scm.mail.spi;

import lombok.AccessLevel;
import lombok.Getter;

import java.util.Locale;
import java.util.Objects;

class Recipient {
  @Getter(value = AccessLevel.PACKAGE)
  private final String address;
  @Getter(value = AccessLevel.PACKAGE)
  private final String userId;
  @Getter(value = AccessLevel.PACKAGE)
  private final Locale locale;
  @Getter(value = AccessLevel.PACKAGE)
  private final String displayName;

  Recipient(String address) {
    this(null, null, address, null);
  }

  Recipient(String displayName, String address) {
    this(null, displayName, address, null);
  }

  Recipient(Locale locale, String address) {
    this(locale, null, address, null);
  }

  Recipient(Locale locale, String displayName, String address) {
    this(locale, displayName, address, null);
  }

  Recipient(Locale locale, String displayName, String address, String userId) {
    this.locale = locale;
    this.displayName = displayName;
    this.address = address;
    this.userId = userId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Recipient that)) return false;
    return address.equals(that.address);
  }

  @Override
  public int hashCode() {
    return Objects.hash(locale, address);
  }
}
