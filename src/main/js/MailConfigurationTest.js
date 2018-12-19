//@flow
import React from "react";
import { translate } from "react-i18next";
import Button from "@scm-manager/ui-components/src/buttons/Button";
import { apiClient } from "@scm-manager/ui-components";

type Props = {
  t: string => string,
  link: string
};

type State = {
  showModal: boolean,
  failure: boolean
};

class MailConfigurationTest extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = {
      showModal: false,
      failure: false
    };
  }

  fetchConfiguration = () => {
    const { link } = this.props;

    const configLink =
      "http://localhost:8081/scm/api/v2/plugins/mail/test/?to=florian.scholdei@cloudogu.com";
    //link + "/test/?to=florianscholdei@gmail.com";
    return apiClient
      .post(configLink)
      .then(response => {
        this.setState({ showModal: true, failure: false });
      })
      .catch(err => {
        this.setState({ showModal: true, failure: true });
      });
  };

  closeModal = () => {
    this.setState({
      showModal: false
    });
  };

  render() {
    const { t } = this.props;
    const { showModal, failure } = this.state;

    let modal = null;
    if (failure) {
      modal = <div className="content">{t("scm-mail-plugin.test.error")}</div>;
    } else {
      modal = (
        <div className="content">{t("scm-mail-plugin.test.success")}</div>
      );
    }

    if (showModal) {
      return (
        <div className="modal is-active">
          <div className="modal-background" />
          <div className="modal-card">
            <header className="modal-card-head">
              <p className="modal-card-title">
                {t("scm-mail-plugin.test.modalTitle")}
              </p>
              <button
                className="delete"
                aria-label="close"
                onClick={() => this.closeModal()}
              />
            </header>
            <section className="modal-card-body">{modal}</section>
          </div>
        </div>
      );
    }

    return (
      <Button
        label={t("scm-mail-plugin.test.button")}
        action={this.fetchConfiguration}
      />
    );
  }
}

export default translate("plugins")(MailConfigurationTest);
