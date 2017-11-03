import React from 'react';
import { connect } from 'react-redux'
import { } from 'prop-types'

import { selectAlertRuleForEditing } from '../../actions/actions'

import AlertRuleList from './alertlist'

const mapStateToProps = state => {
  return {
    alertRulesForFilter : state.alertsForFilter
  }
}

const mapDispatchToProps = dispatch => {
  return {
    selectAlertRuleForEditing : (alertRule) => {
      dispatch(selectAlertRuleForEditing(alertRule))
    }
  }
}

class AlertListContainer extends React.Component {

  constructor(props) {
    super(props)

    this.handleChangeAlertRule = this.handleChangeAlertRule.bind(this)
  }

  handleChangeAlertRule(event) {
    this.props.selectAlertRuleForEditing(event.target.value)
  }

  render() {
    return (
      <AlertRuleList alertRules={this.props.alertRulesForFilter} onChange={this.handleChangeAlertRule}/>
    )
  }
}

export default connect(mapStateToProps, mapDispatchToProps)(AlertListContainer)
