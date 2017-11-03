import React from 'react';
import ServicesTableContainer from '../../components/servicestable/ServicesTableContainer'
import NavBar from '../../components/topnav/NavBar'
import ServiceButtonToolbar from '../../components/servicebutton/servicebuttontoolbar'

import { fetchServices, startServices, stopServices, toggleSelectService } from '../../actions/actions'
import { connect } from 'react-redux'

const mapStateToProps = state => {
  return {
    servicesMap : state.servicesMap,
    selectedServices : state.selectedServices
  }
}

const mapDispatchToProps = dispatch => {
  return {
    fetchServices : () => {
      dispatch(fetchServices());
    },
    startServices : (servicesMap) => {
      dispatch(startServices(dispatch, servicesMap));
    },
    stopServices : (servicesMap) => {
      dispatch(stopServices(dispatch, servicesMap));
    },
    toggleSelectService : (serviceName) => {
      dispatch(toggleSelectService(serviceName))
    }
  }
}

class Admin extends React.Component {
  updateServices() {
    this.props.fetchServices();
    this.timerId = setTimeout(this.updateServices.bind(this), 5000);
  }

  componentWillMount() {
    this.updateServices();
  }

  componentWillUnMount() {
    clearTimeout(this.timerId);
  }

  render() {
    return (
      <div>
        <NavBar title="Admin Dashboard" activeLinkName="Admin"/>
        <ServiceButtonToolbar
          startServices={() => { this.props.startServices(this.props.servicesMap) }}
          stopServices={() => { this.props.stopServices(this.props.servicesMap) }}
        />
        <ServicesTableContainer />
      </div>
    )
  }
}

export default connect(mapStateToProps, mapDispatchToProps)(Admin);
