// @flow
import React from "react";
import { Title, Configuration } from "@scm-manager/ui-components";
import MailConfigurationForm from "./MailConfigurationForm";
import MailConfigurationTest from "./MailConfigurationTest";

type Props = {
  link: string
};

class GlobalMailConfiguration extends React.Component<Props> {
  render() {
    const { link } = this.props;
    return (
      <>
        <Title title="Mail Configuration" />
        <Configuration
          link={link}
          render={props => <MailConfigurationForm {...props} />}
        />
        <MailConfigurationTest link={link} />
      </>
    );
  }
}

export default GlobalMailConfiguration;
