import React from 'react';
import NavBar from '../../components/topnav/NavBar'
import PatientCardsTable from '../../components/patientcards/patientcardstable'

class Home extends React.Component {
  render() {
    return (
      <div>
        <NavBar title="Clinical Dashboard" activeLinkName="Home"/>
        <PatientCardsTable />
      </div>
    )
  }
}

export default Home;
