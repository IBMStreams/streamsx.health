import React from 'react'
import EventToolbar from './eventbar'
import { connect } from 'react-redux'
import { openEditAlertsModal, closeEditAlertsModal, fetchPatients } from '../../actions/actions'


const mapStateToProps = state => {
  return {
    isEditAlertsModalOpen : state.isEditAlertsModalOpen
  }
}

const mapDispatchToProps = dispatch => {
  return {
    openEditAlertsModal : () => {
      dispatch(fetchPatients())
      dispatch(openEditAlertsModal())
    },
    closeEditAlertsModal : () => {
      dispatch(closeEditAlertsModal())
    }
  }
}

class EventToolbarContainer extends React.Component {
  render() {
    return (
      <EventToolbar openEditAlertsModal={this.props.openEditAlertsModal} />
    )
  }
}

export default connect(mapStateToProps, mapDispatchToProps)(EventToolbarContainer)
