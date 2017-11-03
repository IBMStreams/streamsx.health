import React from 'react'
import { connect } from 'react-redux'

import AlertFilter from './alertfilter';
import { fetchAlertsList } from '../../actions/actions'

const mapStateToProps = state => {
  return {
    patients : state.patients
  }
}

const mapDispatchToProps = dispatch => {
  return {
    filterSelected : (filter) => {
      dispatch(fetchAlertsList(filter))
    }
  }
}

class AlertFilterContainer extends React.Component {

  constructor(props) {
    super(props)

    this.handleChange = this.handleChange.bind(this);
  }

  handleChange(event) {
    this.props.filterSelected(event.target.value);
  }

  render() {
    return (
      <AlertFilter patients={this.props.patients} onChange={this.handleChange} />
    )
  }
}

export default connect(mapStateToProps, mapDispatchToProps)(AlertFilterContainer);
