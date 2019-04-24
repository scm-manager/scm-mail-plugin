// @flow
import React from "react";
import { Subtitle, Configuration } from "@scm-manager/ui-components";
import { translate } from "react-i18next";
import UserMailConfigurationForm from "./UserMailConfigurationForm";

type Props = {
  link: string,
  t: string => string
};

class UserMailConfigurationComponent extends React.Component<Props> {
  render() {
    const { t, link } = this.props;
    return (
      <>
        <Subtitle subtitle={t("scm-mail-plugin.form.header")} />
        <Configuration
          link={link}
          render={props => <UserMailConfigurationForm {...props} />}
        />
      </>
    );
  }
}

export default translate("plugins")(UserMailConfigurationComponent);
