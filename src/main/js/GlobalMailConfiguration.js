// @flow
import React from "react";
import { Title, Configuration } from "@scm-manager/ui-components";
import MailConfigurationForm from "./MailConfigurationForm";

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
      </>
    );
  }
}

export default GlobalMailConfiguration;
