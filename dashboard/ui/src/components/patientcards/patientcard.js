import React from 'react'
import PatientModalContainer from './patientmodalcontainer'
import AlertDecorators from './alertdecorators'
import { string, object, func } from 'prop-types'

const myCardStyle = {
  width : "12em",
  marginTop : "20px"
}

const myCardHeaderStyle = {
  backgroundColor : "#c5cae9"
}

const myCardBlockStyle = {
  backgroundColor : "#E1E2E1"
}

const myCardText = {
  font: "14px \"Lucida Grande\", Helvetica, Arial, sans-serif",
  listStyle : "none",
  paddingLeft: 0
}

const liInfoStyle = {
  fontWeight : "bold"
}

const barStyle = {
    height: "5px",
    width: "100%"
}

 const successBarStyle = {
    backgroundColor: "#5cb85c"
}

const warningBarStyle =  {
    backgroundColor: "#f0ad4e"
}

class PatientCard extends React.Component {

  static propTypes = {
    activeAlerts : object.isRequired,
    patientId : string.isRequired,
    source : string.isRequired,
    onCardClick : func.isRequired
  }

  handleClick = () => {
    this.props.onCardClick(this.props.patientId);
  }

  getIcon() {
    switch(this.props.source) {
      case "ambulance":
        return <i className="fa fa-ambulance fa-4x"></i>
      case "mobile":
      case "remote":
        return <i className="fa fa-mobile fa-4x"></i>
      default:
        return <i className="fa fa-mobile fa-4x"></i>
        // return <i className="fa fa-bed fa-4x"></i>
    }
  }

  getDecoratorClassName(vitalName) {
    switch(vitalName) {
      case "abp_sys":
      case "abp_dias":
        return "fa fa-tachometer"
      case "temperature":
        return "fa fa-thermometer"
      case "pulse":
      case "heart_rate":
        return "fa fa-heartbeat"
      case "resp_rate":
        return "fa fa-stethoscope"
      case "spo2":
        return "fa fa-tint"
      default:
        return "fa fa-exclamation-triangle"
    }
  }

  getDecorators() {
    var decorators = []

    var names = []
    for(var idx in this.props.activeAlerts) {
      var vitalName = this.props.activeAlerts[idx].vitalName;
      var decoratorClassName = this.getDecoratorClassName(vitalName);
      if(names.indexOf(decoratorClassName) !== -1) {
        continue;
      }

      names.push(decoratorClassName);
      decorators.push(<div key={vitalName} className="col-2"><i className={decoratorClassName}></i></div>)
    }

    if(decorators.length === 0) {
      decorators.push(<div key={"empty"} className="col-2" style={{opacity : "0"}}><i className="fa fa-check"></i></div>)
    }

    return decorators;
  }

  getOutlineClassName() {
    return Object.keys(this.props.activeAlerts).length === 0 ? "card-outline-success" : "card-outline-warning";
  }

  getStatusBarClassName() {
    return Object.keys(this.props.activeAlerts).length === 0 ? successBarStyle : warningBarStyle;
  }

  render() {
    return (
      <div className="col-2">
        <div className={"card " + this.getOutlineClassName()} style={myCardStyle} onClick={this.handleClick}>
          <div className="card-header" style={myCardHeaderStyle}>
              <AlertDecorators activeAlerts={this.props.activeAlerts} />
              {/* {this.getDecorators()} */}
          </div>
          <div className="card-block" style={myCardBlockStyle}>
            <h4 className="card-title">{this.props.patientId}</h4>
            <div className="text-center" style={{marginBottom : "20px", marginTop: "20px"}}>{this.getIcon()}</div>
            <div className="text-center">
              <i className="fa fa-bars fa-2x" ></i>
            </div>
          </div>
          <div className="align-baseline" style={{...barStyle,...this.getStatusBarClassName()}}>&nbsp;</div>
        </div>
        <PatientModalContainer patientId={this.props.patientId} />
      </div>
    )
  }
}

PatientCard.defaultProps = {
  activeAlerts : {}
}

export default PatientCard;
