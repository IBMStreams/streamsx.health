import React from 'react';
import Modal from 'react-modal';
import {  bool, string, func, object, array } from 'prop-types'
import PatientAnalyticsModalContainer from './analyticsmodalcontainer'
import WaveformGraphContainer from './waveformgraphcontainer'
import VitalTextContainer from './vitaltextcontainer'
import EventChartContainer from '../eventchart/eventchartcontainer'
import ScatterplotContainer from './scatterplotcontainer'
import AlertDecorators from './alertdecorators'

const navStyle = {
  "color" : "#000000"
}

const decoratorBarSuccess = {
}

const decoratorBarWarning = {
  backgroundColor : "#f0ad4e",
  opacity : "0.5"
}

class BasicModalDisplay extends React.Component {
  debugListEnabledServices() {
    var arr = []
    var svcs = this.props.patientServices
    for(var svcName in svcs) {
      if(svcs[svcName].isEnabledForPatient) {
        arr.push(<li key={svcName}>{svcName}</li>)
      }
    }

    return arr;
  }

  render() {
    return (
      <div id="content" className="container-fluid">
        <div className="row">
          <div className="col-6" id="vitalsContainer">
            <div className="row" style={{backgroundColor : "#152935"}}>
              <div className="col-3">
                <VitalTextContainer patientId={this.props.patientId} vitalName="abp_sys"
                  vitalUOM="mmHg" label="ABP Systolic" style={{color : "#fdd600"}}/>
              </div>
              <div className="col-3">
                <VitalTextContainer patientId={this.props.patientId} vitalName="spo2"
                  vitalUOM="%" format={'0.0'} label="SpO2" style={{color : "#8cd211"}}/>
              </div>
              <div className="col-3">
                <VitalTextContainer patientId={this.props.patientId} vitalName="resp_rate"
                  vitalUOM="bpm" label="Resp Rate" style={{color : "#7cc7ff"}}/>
              </div>
              <div className="col-3">
                <VitalTextContainer patientId={this.props.patientId} vitalName="temperature"
                  vitalUOM="&deg;F" format={'0.0'} label="Temperature" style={{color : "#ee82ee"}}/>
              </div>
            </div>
            <div className="row" style={{backgroundColor : "#152935"}}>
              <div className="col-3">
                <VitalTextContainer patientId={this.props.patientId} vitalName="abp_dias" vitalUOM="mmHg"
                  label="ABP Diastolic" style={{color : "#fdd600"}}/>
              </div>
            </div>
            <div className="row">
              <div className="col-12" id="alertsGraphContainer">
                <EventChartContainer patientFilter={[this.props.patientId]} showLegend={false} chartWidth={"100%"} laneHeight={"300"}/>
              </div>
            </div>
          </div>
          <div className="col-6">
            <WaveformGraphContainer chartTitle="ECG I" patientId={this.props.patientId} vitalNames={["ecg_lead_i"]} graphWidth={700} />
            <WaveformGraphContainer chartTitle="ECG II" patientId={this.props.patientId} vitalNames={["ecg_lead_ii"]} graphWidth={700} />
            <WaveformGraphContainer chartTitle="ECG V" patientId={this.props.patientId} vitalNames={["ecg_lead_v"]} graphWidth={700} />
          </div>
          {/* <div className="col-6" id="poincarePlot">
            <ScatterplotContainer patientId={this.props.patientId} vitalNames={["rr_interval"]} />
          </div> */}
        </div>
        <ul>
          {this.debugListEnabledServices()}
        </ul>
      </div>
    )
  }
}

class PatientModal extends React.Component {

  static propTypes = {
    isModalOpen: bool.isRequired,
    patientId: string.isRequired,
    closeModal : func.isRequired,
    openAnalyticsModal : func.isRequired,
    patientServices : object.isRequired,
    displayedVitals : array
  }

  constructor(props) {
    super(props);

    this.afterOpenModal = this.afterOpenModal.bind(this);
    this.closeModal = this.closeModal.bind(this);
    this.handleOpenPatientAnalyticsModal = this.handleOpenPatientAnalyticsModal.bind(this);
    this.getDisplayedVitalGraphs = this.getDisplayedVitalGraphs.bind(this)
    this.getModalDisplay = this.getModalDisplay.bind(this)
  }

  afterOpenModal() {
    // refs are now sync'd and can be accessed
    //this.subtitle.style.color = "#f00";
  }

  closeModal() {
    this.props.closeModal(this.props.patientId);
  }

  handleOpenPatientAnalyticsModal() {
    this.props.openAnalyticsModal(this.props.patientId)
  }

  getDisplayedVitalGraphs() {
    var arr = []
    for(var idx in this.props.displayedVitals) {
      var vitalName = this.props.displayedVitals[idx];
      arr.push(
        <div className="col-6" id={vitalName + "GraphContainer"}>
          <WaveformGraphContainer chartTitle={vitalName} patientId={this.props.patientId} vitalNames={[vitalName]} graphWidth={700} />
        </div>
      )
    }

    return arr;
  }

  getModalDisplay() {
    return <BasicModalDisplay patientId={this.props.patientId} patientServices={this.props.patientServices} />
  }

  render() {
    var activeAlerts = this.props.activeAlerts[this.props.patientId];
    var decoractorRowBGClass = decoratorBarSuccess;
    if(typeof activeAlerts !== 'undefined' && activeAlerts != null && Object.keys(activeAlerts).length > 0) {
      decoractorRowBGClass = decoratorBarWarning;
    }

    return (
      <Modal
        isOpen={this.props.isModalOpen}
        onAfterOpen={this.afterOpenModal}
        onRequestClose={this.closeModal}
        contentLabel="Patient Info"
        style={{
          content : {
            overflow : "hidden",
            overflowY : "auto"
          }
        }}
      >
        <div id="modalHeader">
          <div className="container-fluid">
            <div className="row justify-content-start">
              <div className="col-9">
                <h2>{this.props.patientId}</h2>
              </div>
              <div className="col-2">
                <AlertDecorators activeAlerts={activeAlerts} iconSize={2}/>
              </div>
              <div className="col-1 text-right">
                <button className="close" onClick={this.closeModal}><span>&times;</span></button>
              </div>
            </div>
          </div>
          <nav className="navbar navbar-toggleable-md navbar-inverse" style={navStyle}>
            <div><button className="btn btn-secondary" onClick={this.handleOpenPatientAnalyticsModal}><i className="fa fa-cog"></i> Configure</button></div>
          </nav>
          <hr />
        </div>
        <div id="modalBody">
            {this.getModalDisplay()}
        </div>
        <PatientAnalyticsModalContainer patientId={this.props.patientId} />
      </Modal>
    )
  }
}

PatientModal.defaultProps = {
  displayedVitals : []
}

export default PatientModal;
