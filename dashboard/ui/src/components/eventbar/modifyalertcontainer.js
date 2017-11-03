import React from 'react'
import { connect } from 'react-redux'
import ModifyAlert from './modifyalert'

import { addAlertRule } from '../../actions/actions'

const mapStateToProps = state => {
  return {
    alertRuleForEditing : state.alertRuleForEditing,
    alertRulesFilter : state.alertRulesFilter
  }
};

const mapDispatchToProps = dispatch => {
  return {
    addAlertRule : (oldAlertRule, newAlertRule) => {
      dispatch(addAlertRule(oldAlertRule, newAlertRule))
    }
  }
}

class ModifyAlertContainer extends React.Component {
  constructor(props) {
    super(props)

    this.handleSave = this.handleSave.bind(this)
  }

  handleSave(oldAlertRule, newAlertRule) {
    if(!newAlertRule.hasOwnProperty('patientId')) {
      newAlertRule.patientId = this.props.alertRulesFilter
    }

    this.props.addAlertRule(oldAlertRule, newAlertRule);
  }

  render() {
    return (
      <ModifyAlert alertRule={this.props.alertRuleForEditing} onSave={this.handleSave}/>
    )
  }
}

export default connect(mapStateToProps, mapDispatchToProps)(ModifyAlertContainer)
