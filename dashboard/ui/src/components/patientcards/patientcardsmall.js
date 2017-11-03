import React from 'react'
import PatientModalContainer from './patientmodalcontainer'
import ReactTooltip from 'react-tooltip'
import { string, object, func } from 'prop-types'

const myCardStyle = {
  width : "4em",
  marginTop : "20px"
}

const cardStyle = {
  marginTop : "20px",
  marginRight : "2px",
  marginLeft : "2px"
}

const squareCardStyle = {
  "width" : "2em",
  "height" : "2em",
  backgroundColor : "#E1E2E1",
  borderRadius : "5px",
  verticalAlign : "middle",
  textAlign : "center"
}

const squareCardBorderSuccess = {
  border : "2px solid #5cb85c"
}

const squareCardBorderWarning = {
  border : "2px solid #f0ad4e"
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

class PatientCardSmall extends React.Component {

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
    var clazzName;
    switch(this.props.source) {
      case "ambulance":
        clazzName = "fa fa-ambulance";
        break;
      case "mobile":
      case "remote":
        clazzName="fa fa-mobile";
        break;
      default:
        clazzName="fa fa-mobile";
        // clazzName="fa fa-bed";
    }

    clazzName += " align-middle";
    return <i className={clazzName} style={{color : "#387038"}}></i>
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

    return <div className="row">{decorators}</div>;
  }

  getOutlineClassName() {
    return Object.keys(this.props.activeAlerts).length === 0 ? "card-outline-success" : "card-outline-warning";
  }

  getBorderStyle() {
    return Object.keys(this.props.activeAlerts).length === 0 ? squareCardBorderSuccess : squareCardBorderWarning;
  }

  getStatusBarClassName() {
    return Object.keys(this.props.activeAlerts).length === 0 ? successBarStyle : warningBarStyle;
  }

  render() {
    var cStyle = {...squareCardStyle, ...(this.getBorderStyle())}
    var tooltipTheme = Object.keys(this.props.activeAlerts).length === 0 ? "success" : "warning";
    return (
      <div className="col-auto" style={cardStyle}>
        <div onClick={this.handleClick} data-tip data-for={this.props.patientId} style={cStyle}>
          {this.getIcon()}
        </div>
        <PatientModalContainer patientId={this.props.patientId} />
        <ReactTooltip type={tooltipTheme} id={this.props.patientId}>{this.props.patientId}{this.getDecorators()}</ReactTooltip>
      </div>
    )
  }
}

PatientCardSmall.defaultProps = {
  activeAlerts : {}
}

export default PatientCardSmall;
