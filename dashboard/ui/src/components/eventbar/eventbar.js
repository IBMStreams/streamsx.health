import React from 'react'
import { func, bool } from 'prop-types'
import EditAlertsModalContainer from './editalertsmodalcontainer'

const navStyle = {
  backgroundColor : "#ffffff",
  "color" : "#000000"
}

class EventToolbar extends React.Component {

  constructor(props) {
    super(props)

    this.handleOpenEditAlertsModal = this.handleOpenEditAlertsModal.bind(this)
  }

  static propTypes = {
    openEditAlertsModal : func.isRequired,
  }

  handleOpenEditAlertsModal() {
    this.props.openEditAlertsModal();
  }

  render() {
    return (
      <nav className="navbar navbar-toggleable-md navbar-inverse" style={navStyle}>
        <div style={{"width" : "10px"}}></div>
        <div className="collapse navbar-collapse">
          <ul className="navbar-nav">
            <button className="btn btn-secondary" onClick={this.handleOpenEditAlertsModal}><i className="fa fa-edit"></i> Edit Alerts</button>
            <EditAlertsModalContainer />
          </ul>
        </div>
      </nav>
    )
  }
}

export default EventToolbar;
