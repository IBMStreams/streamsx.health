import React from 'react'

const EMPTY_ALERT_RULE = {
  ruleName : "",
  vitalName : "",
  minInclusive : "",
  maxExclusive : "",
  duration : ""
}

class AlertRuleList extends React.Component  {

  getOptions() {
    let options = []
    var emptyAlertRule = JSON.stringify(EMPTY_ALERT_RULE)
    options.push(<option value={emptyAlertRule} key="new_alert">New Alert...</option>)

    for(var idx in this.props.alertRules) {
      var ruleStr = this.props.alertRules[idx];
      var rule = JSON.parse(this.props.alertRules[idx])
      options.push(<option value={ruleStr} key={rule.ruleName}>{rule.ruleName}</option>)
    }

    return options;
  }

  render() {
    return (
      <select size="15" onChange={this.props.onChange}>
        {this.getOptions()}
      </select>
    )
  }
}

export default AlertRuleList;
