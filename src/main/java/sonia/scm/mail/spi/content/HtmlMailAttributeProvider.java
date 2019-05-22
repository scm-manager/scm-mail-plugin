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
  private static final String LI_STYLE = DEFAULT;
  private static final String PRE_STYLE = DEFAULT;
  private static final String CODE_STYLE = PRE_STYLE;
  private static final String IMG_STYLE = "margin: 0 auto; padding: 0; font-size: 100%; " + FONT_FAMILY + " line-height: 1.65; max-width: 100%; display: block;";
  private static final String BLOCKQUOTE_STYLE = DEFAULT;
  private static final String HR_STYLE = DEFAULT;

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
