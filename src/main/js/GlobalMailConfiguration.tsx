import React from "react";
import { Title, Configuration } from "@scm-manager/ui-components";
import MailConfigurationForm from "./MailConfigurationForm";
import { withTranslation, WithTranslation } from "react-i18next";

type Props = WithTranslation & {
  link: string;
};

class GlobalMailConfiguration extends React.Component<Props> {
  render() {
    const { t, link } = this.props;
    return (
      <>
        <Title title={t("scm-mail-plugin.form.header")} />
        <Configuration link={link} render={props => <MailConfigurationForm {...props} />} />
      </>
    );
  }
}

export default withTranslation("plugins")(GlobalMailConfiguration);
