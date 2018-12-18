package sonia.scm.mail;

import com.google.inject.AbstractModule;
import org.mapstruct.factory.Mappers;
import sonia.scm.mail.internal.MailConfigurationMapper;
import sonia.scm.plugin.Extension;

@Extension
public class MailConfigurationModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(MailConfigurationMapper.class).to(Mappers.getMapper(MailConfigurationMapper.class).getClass());
  }
}
