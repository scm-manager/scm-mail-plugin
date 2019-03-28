// @flow
import React from "react";
import { NavLink } from "@scm-manager/ui-components";
import { translate } from "react-i18next";

type Props = {
  url: string,
  t: string => string
};

class UserMailConfigurationNavLink extends React.Component<Props> {
  render() {
    const { url, t } = this.props;

    return (
      <NavLink
        to={`${url}/settings/mail-configuration`}
        label={t("scm-mail-plugin.nav-link")}
      />
    );
  }
}

export default translate("plugins")(UserMailConfigurationNavLink);
