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
