package sonia.scm.mail.internal;

import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Links;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
public class MailConfigurationDto extends HalRepresentation {
  private String host;
  private int port;
  private String transportStrategy;
  private String username;
  private String password;
  private String from;
  private String subjectPrefix;
  private String language;

  @Override
  @SuppressWarnings("squid:S1185") // We want to have this method available in this package
  protected HalRepresentation add(Links links) {
    return super.add(links);
  }
}
