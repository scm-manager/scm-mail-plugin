//@flow
import React from "react";
import { Title, GlobalConfiguration } from "@scm-manager/ui-components";
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
        <GlobalConfiguration link={link} render={props => <MailConfigurationForm {...props} />} />
      </>
    );
  }

}

export default GlobalMailConfiguration;
