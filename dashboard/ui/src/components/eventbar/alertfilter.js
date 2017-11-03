import React from 'react'

function getFilterOptions(props) {
  let filterOptions = []
  filterOptions.push(<option key="none" disabled value="none">select a filter</option>)
  filterOptions.push(<option key="global" value="*">global</option>);

  if(typeof props.patients === 'undefined')
    return filterOptions;

  var sortedPatients = props.patients.slice().sort();
  for(var idx in sortedPatients) {
    var patientId = sortedPatients[idx]
    filterOptions.push(<option key={patientId} value={patientId}>{patientId}</option>)
  }

  return filterOptions;
}

function AlertFilter(props) {
  return (
    <select onChange={props.onChange} defaultValue="none">
      {getFilterOptions(props)}
    </select>
  )
};

export default AlertFilter;
