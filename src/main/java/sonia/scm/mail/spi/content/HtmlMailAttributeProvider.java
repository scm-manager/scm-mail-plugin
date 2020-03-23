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
package sonia.scm.mail.spi.content;

import com.google.common.collect.ImmutableMap;
import org.commonmark.node.Node;
import org.commonmark.renderer.html.AttributeProvider;

import java.util.Map;

class HtmlMailAttributeProvider implements AttributeProvider {

  private static final String FONT_FAMILY = "font-family: 'Avenir Next', 'Helvetica Neue', 'Helvetica', Helvetica, Arial, sans-serif;";
  private static final String PREFIX = "margin: 0; padding: 0; " + FONT_FAMILY + " ";
  private static final String DEFAULT = PREFIX + "font-size: 100%; line-height: 1.65;";

  private static final String H_STYLE = PREFIX + "font-size: %s; line-height: 1.25; margin-bottom: 20px;";
  private static final String P_STYLE = PREFIX + "font-size: 16px; line-height: 1.65; font-weight: normal; margin-bottom: 20px;";
  private static final String A_STYLE = PREFIX + "font-size: 100%; line-height: 1.65; color: #33b2e8; text-decoration: none;";
  private static final String OL_STYLE = P_STYLE;
  private static final String UL_STYLE = P_STYLE;
  private static final String LI_STYLE = DEFAULT + " margin-left: 20px;";
  private static final String PRE_STYLE = FONT_FAMILY + " margin: 20px auto; padding: 0 16px; background-color: #ccecf9; width: 510px; overflow-x: scroll;";
  private static final String CODE_STYLE = DEFAULT + " width: 510px;";
  private static final String IMG_STYLE = "margin: 20px auto; padding: 0; font-size: 100%; " + FONT_FAMILY + " line-height: 1.65; max-width: 100%; display: block;";
  private static final String BLOCKQUOTE_STYLE = DEFAULT;
  private static final String HR_STYLE = "margin: 20px auto; padding: 0; " + FONT_FAMILY + " ";

  private static final Map<String,StyleProvider> styleProviders = ImmutableMap.<String,StyleProvider>builder()
    .put("h1", () -> String.format(H_STYLE, "32px"))
    .put("h2", () -> String.format(H_STYLE, "28px"))
    .put("h3", () -> String.format(H_STYLE, "24px"))
    .put("h4", () -> String.format(H_STYLE, "20px"))
    .put("h5", () -> String.format(H_STYLE, "16px"))
    .put("h6", () -> String.format(H_STYLE, "100%"))
    .put("p", () -> P_STYLE)
    .put("a", () -> A_STYLE)
    .put("pre", () -> PRE_STYLE)
    .put("code", () -> CODE_STYLE)
    .put("ul", () -> UL_STYLE)
    .put("ol", () -> OL_STYLE)
    .put("li", () -> LI_STYLE)
    .put("img", () -> IMG_STYLE)
    .put("blockquote", () -> BLOCKQUOTE_STYLE)
    .put("hr", () -> HR_STYLE)
    .build();

  @Override
  public void setAttributes(Node node, String tagName, Map<String, String> attributes) {
    StyleProvider styleProvider = styleProviders.get(tagName);
    if (styleProvider != null) {
      attributes.put("style", styleProvider.createStyle());
    }
  }

  @FunctionalInterface
  private interface StyleProvider {

    String createStyle();

  }
}
