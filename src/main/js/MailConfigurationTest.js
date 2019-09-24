//@flow
import React from "react";
import { translate } from "react-i18next";
import {
  Modal,
  Button,
  Notification,
  InputField
} from "@scm-manager/ui-components";
import { apiClient, validation as validator } from "@scm-manager/ui-components";
import type { MailConfiguration } from "./MailConfiguration";

type Props = {
  configuration: MailConfiguration,
  t: string => string
};

type State = {
  showModal: boolean,
  failure?: Error,
  mail: string,
  loading: boolean,
  mailValidationError?: Error
};

class MailConfigurationTest extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = {
      ...this.state,
      showModal: false,
      loading: false
    };
  }

  updateEmail = (value: string) => {
    this.setState({
      mailValidationError: !validator.isMailValid(value),
      mail: value
    });
  };

  testConfiguration = () => {
    const { configuration } = this.props;
    const { mail } = this.state;
    const configLink = configuration._links.test.href + "?to=" + mail;

    this.setState({ loading: true });

    return apiClient
      .post(configLink, configuration)
      .then(response => {
        this.setState({ showModal: true, failure: null, loading: false });
      })
      .catch(err => {
        this.setState({ showModal: true, failure: err, loading: false });
      });
  };

  closeModal = () => {
    this.setState({
      showModal: false
    });
  };

  renderModal = () => {
    const { t } = this.props;

    const footer = (
      <div className="field is-grouped">
        <p className="control">
          <Button
            label={t("scm-mail-plugin.test.close")}
            action={() => this.closeModal()}
          />
        </p>
      </div>
    );

    return (
      <Modal
        title={t("scm-mail-plugin.test.title")}
        closeFunction={() => this.closeModal()}
        body={this.renderModalContent()}
        footer={footer}
        active={true}
      />
    );
  };

  renderModalContent = () => {
    const { t } = this.props;
    const { failure } = this.state;

    return (
      <Notification type={failure ? "danger" : "success"}>
        {failure
          ? t("scm-mail-plugin.test.error")
          : t("scm-mail-plugin.test.success")}
      </Notification>
    );
  };

  render() {
    const { t } = this.props;
    const { showModal, loading } = this.state;

    let modal = null;
    if (showModal) {
      modal = this.renderModal();
    }

    return (
      <>
        {modal}
        <hr />
        <InputField
          label={t("scm-mail-plugin.test.title")}
          placeholder={t("scm-mail-plugin.test.input")}
          type="email"
          validationError={this.state.mailValidationError}
          onChange={this.updateEmail}
          errorMessage={t("scm-mail-plugin.mailValidationError")}
        />
        <Button
          label={t("scm-mail-plugin.test.button")}
          action={this.testConfiguration}
          loading={loading}
        />
      </>
    );
  }
}

export default translate("plugins")(MailConfigurationTest);
