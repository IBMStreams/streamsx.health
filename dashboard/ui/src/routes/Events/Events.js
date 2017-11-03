import React from 'react';
import NavBar from '../../components/topnav/NavBar'
import EventChartContainer from '../../components/eventchart/eventchartcontainer'
import EventToolbarContainer from '../../components/eventbar/eventbarcontainer'
import { connect } from 'react-redux'

const mapStateToProps = state => {
  return {
    // nothing for now
  }
}

const mapDispatchToProps = dispatch => {
  return {
    // nothing for now
  }
}

class Events extends React.Component {
  render() {
    return (
      <div>
        <NavBar title="Events View" activeLinkName="Events"/>
        <EventToolbarContainer />
        <EventChartContainer />
      </div>
    )
  }
}

export default connect(mapStateToProps, mapDispatchToProps)(Events);
