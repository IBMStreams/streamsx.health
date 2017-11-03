import React from 'react'
import Modal from 'react-modal'
import { bool, string, func, object } from 'prop-types'

import AlertListContainer from './alertlistcontainer'
import ModifyAlertContainer from './modifyalertcontainer'
import AlertFilterContainer from './alertfiltercontainer'

const modalStyle = {
  content : {
    width: "50%",
    left: "25%",
    top: "10%",
    right: "auto"
  }
};

const modalWrapper = {
  minHeight: "100%",
  height: "auto !important",
  marginBottom: "-30px"
}

const modalBodyStyle = {
  height: "100%"
}

const modalFooterStyle = {
  height: "30px",
  textAlign: "right"
}


class EditAlertsModal extends React.Component {

  static propTypes = {
    isModalOpen: bool.isRequired,
    //patientId: string.isRequired,
    closeModal : func.isRequired,
    //patientServices: object.isRequired,
    //saveModal: func.isRequired
  }

  constructor(props) {
    super(props)

    this.closeModal = this.closeModal.bind(this)
  }

  closeModal() {
    this.props.closeModal();
  }

  render() {
    return (
      <Modal
        isOpen={this.props.isModalOpen}
        onAfterOpen={this.afterOpenModal}
        onRequestClose={this.closeModal}
        style={modalStyle}
        contentLabel="Configure Analytics"
      >
        <div style={modalWrapper}>
          <div id="modalHeader">
            <button className="close" onClick={this.closeModal}><span>&times;</span></button>
            <h2>Edit Alerts</h2>
            <hr />
          </div>
          <div id="modalBody" style={modalBodyStyle}>
            <div className="container">
              <div className="row">
                <div className="col-12" style={{marginBottom : "20px"}}>
                  <span>Filter: </span><AlertFilterContainer />
                </div>
              </div>
              <div className="row">
                <div className="col-4">
                  <AlertListContainer />
                </div>
                <div className="col-8">
                  <ModifyAlertContainer />
                </div>
              </div>
            </div>
          </div>
        </div>
        <div id="modalFooter" style={modalFooterStyle}>
        </div>
      </Modal>
    )
  }
}

export default EditAlertsModal;
