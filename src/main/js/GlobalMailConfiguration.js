// @flow
import React from "react";
import { Title, Configuration } from "@scm-manager/ui-components";
import MailConfigurationForm from "./MailConfigurationForm";
import { translate } from "react-i18next";

type Props = {
  link: string,
  t: string => string
};

class GlobalMailConfiguration extends React.Component<Props> {
  render() {
    const { t, link } = this.props;
    return (
      <>
        <Title title={t("scm-mail-plugin.form.header")} />
        <Configuration
          link={link}
          render={props => <MailConfigurationForm {...props} />}
        />
      </>
    );
  }
}

export default translate("plugins")(GlobalMailConfiguration);
