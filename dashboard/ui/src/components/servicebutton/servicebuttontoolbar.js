import React from 'react';
import ServiceButton from '../../components/servicebutton/servicebutton'

const serviceButtonToolbarStyle = {
  marginTop: "20px"
}

class ServiceButtonToolbar extends React.Component {
  render() {
    return (
      <div className="text-center" style={serviceButtonToolbarStyle}>
        <ServiceButton onClick={this.props.startServices} className="btn-success" name="Start Services" />
        <ServiceButton onClick={this.props.stopServices} className="btn-danger" name="Stop Services" />
      </div>
    )
  }
}

export default ServiceButtonToolbar;
