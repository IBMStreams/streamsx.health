import React from 'react'
import { object } from 'prop-types'

class AlertDecorators extends React.Component {

  static propTypes = {
    activeAlerts : object.isRequired
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

    var iconSize = "fa-" + this.props.iconSize + "x";

    var names = []
    for(var idx in this.props.activeAlerts) {
      var vitalName = this.props.activeAlerts[idx].vitalName;
      var decoratorClassName = this.getDecoratorClassName(vitalName);
      if(names.indexOf(decoratorClassName) !== -1) {
        continue;
      }

      names.push(decoratorClassName);
      decorators.push(<div key={vitalName} className="col-2"><i className={decoratorClassName + " " + iconSize}></i></div>)
    }

    if(decorators.length === 0) {
      decorators.push(<div key={"empty"} className="col-2" style={{opacity : "0"}}><i className={"fa fa-check " + iconSize}></i></div>)
    }

    return decorators;
  }

  render() {
    return (
      <div className="row">{this.getDecorators()}</div>
    )
  }
}

AlertDecorators.defaultProps = {
  activeAlerts : {},
  iconSize : 1
}

export default AlertDecorators;
