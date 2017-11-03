import React from 'react'
import ServicesTable from './ServicesTable'
import { toggleSelectService } from '../../actions/actions'
import { connect } from 'react-redux'

const mapStateToProps = state => {
 return {
   servicesMap : state.servicesMap,
   selectedServices : state.selectedServices
 }
}

const mapDispatchToProps = dispatch => {
  return {
    onServiceClick : serviceName => {
      dispatch(toggleSelectService(serviceName))
    }
  }
}

var ServicesTableContainer = (props) => {
  console.log("HERE:", props.servicesMap)
  return (
    <ServicesTable servicesMap={props.servicesMap} selectedServices={props.selectedServices} onServiceClick={props.onServiceClick}/>
  )
}

export default connect(mapStateToProps, mapDispatchToProps)(ServicesTableContainer)
