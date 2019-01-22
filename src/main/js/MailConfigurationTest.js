//@flow
import React from "react";
import { translate } from "react-i18next";
import { Button, InputField } from "@scm-manager/ui-components";
import {
  apiClient,
  validation as validator
} from "@scm-manager/ui-components";
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
    return (
      <div className="modal is-active">
        <div className="modal-background" />
        <div className="modal-card">
          <header className="modal-card-head">
            <p className="modal-card-title">
              {t("scm-mail-plugin.test.title")}
            </p>
            <button
              className="delete"
              aria-label="close"
              onClick={() => this.closeModal()}
            />
          </header>
          {this.renderModalContent()}
        </div>
      </div>
    );
  };

  renderModalContent = () => {
    const { t } = this.props;
    const { failure } = this.state;
    if (failure) {
      return (
        <section className="modal-card-body has-background-danger">
          <div className="content has-text-white">
            {t("scm-mail-plugin.test.error")}
          </div>
        </section>
      );
    }

    return (
      <section className="modal-card-body has-background-success">
        <div className="content has-text-white">
          {t("scm-mail-plugin.test.success")}
        </div>
      </section>
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
          errorMessage={t("scm-mail-plugin.test.mailValidationError")}
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
