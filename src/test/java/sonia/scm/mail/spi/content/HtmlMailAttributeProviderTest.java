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
