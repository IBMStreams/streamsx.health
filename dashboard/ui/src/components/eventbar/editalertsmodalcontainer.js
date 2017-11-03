import React from 'react'
import EditAlertsModal from './editalertsmodal'
import { closeEditAlertsModal } from '../../actions/actions'
import { connect } from 'react-redux'

const mapStateToProps = state => {
  return {
    isEditAlertsModalOpen : state.isEditAlertsModalOpen,
  }
};

const mapDispatchToProps = dispatch => {
  return {
    closeEditAlertsModal : () => {
      dispatch(closeEditAlertsModal());
    }
  }
};

class EditAlertsModalContainer extends React.Component {

  render() {
    return (
      <EditAlertsModal isModalOpen={this.props.isEditAlertsModalOpen} closeModal={this.props.closeEditAlertsModal} />
    )
  }
}

export default connect(mapStateToProps, mapDispatchToProps)(EditAlertsModalContainer)
