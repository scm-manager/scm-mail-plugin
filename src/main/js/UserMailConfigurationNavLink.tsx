import React from "react";
import { NavLink } from "@scm-manager/ui-components";
import { withTranslation, WithTranslation } from "react-i18next";

type Props = WithTranslation & {
  url: string;
};

class UserMailConfigurationNavLink extends React.Component<Props> {
  render() {
    const { url, t } = this.props;

    return <NavLink to={`${url}/settings/mail-configuration`} label={t("scm-mail-plugin.navLink")} />;
  }
}

export default withTranslation("plugins")(UserMailConfigurationNavLink);
