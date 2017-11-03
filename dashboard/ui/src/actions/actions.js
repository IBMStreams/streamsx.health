import axios from 'axios'

export const TOGGLE_SERVICE = 'TOGGLE_SERVICE'

export const START_SERVICES = 'START_SERVICES'
export const START_SERVICES_FINISHED = 'START_SERVICES_FINISHED'
export const START_SERVICES_ERROR = 'START_SERVICES_ERROR'

export const STOP_SERVICES = 'STOP_SERVICES'
export const STOP_SERVICES_FINISHED = 'STOP_SERVICES_FINISHED'
export const STOP_SERVICES_ERROR = 'STOP_SERVICES_ERROR'

export const FETCH_SERVICES = 'FETCH_SERVICES'
export const FETCH_SERVICES_PENDING = 'FETCH_SERVICES_PENDING'
export const FETCH_SERVICES_FULFILLED = 'FETCH_SERVICES_FULFILLED'
export const FETCH_SERVICES_REJECTED = 'FETCH_SERVICES_REJECTED'

export const FETCH_PATIENTS = 'FETCH_PATIENTS'
export const FETCH_PATIENTS_PENDING = 'FETCH_PATIENTS_PENDING'
export const FETCH_PATIENTS_FULFILLED = 'FETCH_PATIENTS_FULFILLED'
export const FETCH_PATIENTS_REJECTED = 'FETCH_PATIENTS_REJECTED'

export const OPEN_PATIENT_MODAL = 'OPEN_PATIENT_MODAL'
export const CLOSE_PATIENT_MODAL = 'CLOSE_PATIENT_MODAL'

export const FETCH_ENABLED_SERVICES_FOR_PATIENT = 'FETCH_ENABLED_SERVICES_FOR_PATIENT'
export const FETCH_ENABLED_SERVICES_FOR_PATIENT_PENDING = 'FETCH_ENABLED_SERVICES_FOR_PATIENT_PENDING'
export const FETCH_ENABLED_SERVICES_FOR_PATIENT_FULFILLED = 'FETCH_ENABLED_SERVICES_FOR_PATIENT_FULFILLED'
export const FETCH_ENABLED_SERVICES_FOR_PATIENT_REJECTED = 'FETCH_ENABLED_SERVICES_FOR_PATIENT_REJECTED'

export const OPEN_PATIENT_ANALYTICS_MODAL = 'OPEN_PATIENT_ANALYTICS_MODAL'
export const OPEN_PATIENT_ANALYTICS_MODAL_PENDING = 'OPEN_PATIENT_ANALYTICS_MODAL_PENDING'
export const OPEN_PATIENT_ANALYTICS_MODAL_FULFILLED = 'OPEN_PATIENT_ANALYTICS_MODAL_FULFILLED'
export const OPEN_PATIENT_ANALYTICS_MODAL_REJECTED = 'OPEN_PATIENT_ANALYTICS_MODAL_REJECTED'
export const CLOSE_PATIENT_ANALYTICS_MODAL = 'CLOSE_PATIENT_ANALYTICS_MODAL'

export const UPDATE_SERVICES_FOR_PATIENT = 'UPDATE_ENABLED_SERVICES_FOR_PATIENT'
export const UPDATE_SERVICES_FOR_PATIENT_PENDING = 'UPDATE_ENABLED_SERVICES_FOR_PATIENT_PENDING'
export const UPDATE_SERVICES_FOR_PATIENT_FULFILLED = 'UPDATE_ENABLED_SERVICES_FOR_PATIENT_FULFILLED'
export const UPDATE_SERVICES_FOR_PATIENT_REJECTED = 'UPDATE_ENABLED_SERVICES_FOR_PATIENT_REJECTED'

export const UPDATE_ALERTS = 'UPDATE_ALERTS'
export const UPDATE_ALERTS_PENDING = 'UPDATE_ALERTS_PENDING'
export const UPDATE_ALERTS_FULFILLED = 'UPDATE_ALERTS_FULFILLED'
export const UPDATE_ALERTS_REJECTED = 'UPDATE_ALERTS_REJECTED'

export const OPEN_EDIT_ALERTS_MODAL = 'OPEN_EDIT_ALERTS_MODAL'
export const CLOSE_EDIT_ALERTS_MODAL = 'CLOSE_EDIT_ALERTS_MODAL'

export const FETCH_ALERT_RULES = 'FETCH_ALERT_RULES'
export const FETCH_ALERT_RULES_PENDING = 'FETCH_ALERT_RULES_PENDING'
export const FETCH_ALERT_RULES_FULFILLED = 'FETCH_ALERT_RULES_FULFILLED'
export const FETCH_ALERT_RULES_REJECTED = 'FETCH_ALERT_RULES_REJECTED'

export const ALERT_RULE_SELECTED_FOR_EDITING = 'ALERT_RULE_SELECTED_FOR_EDITING'

export const ADD_ALERT_RULE = 'ADD_ALERT_RULE'
export const ADD_ALERT_RULE_PENDING = 'ADD_ALERT_RULE_PENDING'
export const ADD_ALERT_RULE_FULFILLED = 'ADD_ALERT_RULE_FULFILLED'
export const ADD_ALERT_RULE_REJECTED = 'ADD_ALERT_RULE_REJECTED'

export const FETCH_PATIENT_VITALS_DATA = 'FETCH_PATIENT_VITALS_DATA'
export const FETCH_PATIENT_VITALS_DATA_PENDING = 'FETCH_PATIENT_VITALS_DATA_PENDING'
export const FETCH_PATIENT_VITALS_DATA_FULFILLED = 'FETCH_PATIENT_VITALS_DATA_FULFILLED'
export const FETCH_PATIENT_VITALS_DATA_REJECTED = 'FETCH_PATIENT_VITALS_DATA_REJECTED'

export const FETCH_PATIENT_ACTIVE_ALERTS = 'FETCH_PATIENT_ACTIVE_ALERTS'
export const FETCH_PATIENT_ACTIVE_ALERTS_PENDING = 'FETCH_PATIENT_ACTIVE_ALERTS_PENDING'
export const FETCH_PATIENT_ACTIVE_ALERTS_FULFILLED = 'FETCH_PATIENT_ACTIVE_ALERTS_FULFILLED'
export const FETCH_PATIENT_ACTIVE_ALERTS_REJECTED = 'FETCH_PATIENT_ACTIVE_ALERTS_REJECTED'

export const UPDATE_DISPLAYED_VITALS = 'UPDATE_DISPLAYED_VITALS'

export const FETCH_AVAILABLE_PATIENT_VITALS = 'FETCH_AVAILABLE_PATIENT_VITALS'
export const FETCH_AVAILABLE_PATIENT_VITALS_PENDING = 'FETCH_AVAIABLE_PATIENT_VITALS'
export const FETCH_AVAILABLE_PATIENT_VITALS_FULFILLED = 'FETCH_AVAILABLE_PATIENT_VITALS_FULFILLED'
export const FETCH_AVAILABLE_PATIENT_VITALS_REJECTED = 'FETCH_AVAILALBE_PATIENT_VITALS_REJECTED'

export const SET_PATIENT_CARD_SIZE = 'SET_PATIENT_CARD_SIZE'

export const SET_MODAL_DISPLAY_TYPE = 'SET_MODEL_DISPLAY_TYPE'

export const SET_PATIENT_CARDS_FILTER_TYPES = 'SET_PATIENT_CARDS_FILTER_TYPES'

export function setPatientCardsFilterTypes(patientCardsFilterTypes) {
  return {
    type : SET_PATIENT_CARDS_FILTER_TYPES,
    patientCardsFilterTypes
  }
}


export function setModalDisplayType(modalDisplayType) {
  return {
    type : SET_MODAL_DISPLAY_TYPE,
    modalDisplayType
  }
}

export function setPatientCardSize(cardSize) {
  return {
    type : SET_PATIENT_CARD_SIZE,
    cardSize
  }
}

export function fetchAvailablePatientVitals(patientId) {
  return {
    type: FETCH_AVAILABLE_PATIENT_VITALS,
    payload : axios.get('/data/' + patientId + '/data-types')
                   .then(response => {
                     return {
                       availableVitals : response.data,
                       patientId
                     }
                   })
  }
}

export function updateDisplayedVitals(patientId, vitalNames) {
  return {
    type: UPDATE_DISPLAYED_VITALS,
    patientId,
    vitalNames
  }
}

export function fetchPatientActiveAlerts() {
  return {
    type: FETCH_PATIENT_ACTIVE_ALERTS,
    payload : axios.get('/alerts/active')
                   .then(response => {
                     return response.data
                   })
  }
}

export function updatePatientVitalsData(patientId, vitalNames, start, end) {
  var queryStr = ""

  for(var idx in vitalNames) {
    queryStr += "&vitalName=" + vitalNames[idx]
  }

  var restURL = '/data/' + patientId + '?start=' + '&start=' + start + '&end=' + end + queryStr
  console.log("[updatePatientVitalsData] ", restURL)
  return {
    type : FETCH_PATIENT_VITALS_DATA,
    payload : axios.get(restURL)
                   .then(response => {
                     return {
                       data : response.data,
                       vitalNames,
                       start,
                       end
                     }
                   }),
    vitalNames
  }
}

export function addAlertRule(oldAlertRule, newAlertRule) {
  var params = new URLSearchParams();
  params.append('newAlertRule', JSON.stringify(newAlertRule));

  if(typeof oldAlertRule !== 'undefined') {
    params.append('oldAlertRule', JSON.stringify(oldAlertRule))
  }

  console.log("PARAMS: ", params.toString());
  console.log("OLD ALERT RULE:", oldAlertRule)
  console.log("NEW ALERT RULE:", newAlertRule)

  return {
    type : ADD_ALERT_RULE,
    payload : axios.post('alert-rules/add', params)
                   .then(response => {
                     return {
                       data : response.data
                     }
                   })
  }
}

export function selectAlertRuleForEditing(alertRule) {
  return {
    type : ALERT_RULE_SELECTED_FOR_EDITING,
    alertRuleForEditing : alertRule
  }
}

export function fetchAlertsList(patientId) {
  var restURL = (typeof patientId === 'undefined') ? '/alert-rules' : '/alert-rules?patientId=' + patientId
  return {
    type : FETCH_ALERT_RULES,
    payload : axios.get(restURL)
                   .then(response => {
                     return {
                       data : response.data,
                       alertRulesFilter : patientId
                     }
                   })
  };
}

export function openEditAlertsModal() {
  return {
    type : OPEN_EDIT_ALERTS_MODAL
  }
}

export function closeEditAlertsModal() {
  return {
    type : CLOSE_EDIT_ALERTS_MODAL
  }
}

export function updateAlerts(startTime=0, endTime=-1) {
  return {
    type: UPDATE_ALERTS,
    payload : axios.get('/alerts?startTime=' + startTime + "&endTime=" + endTime)
                   .then(response => {
                     return {
                       data : response.data
                     }
                   })
  };
}

export function updatePatientServices(patientId, changedServices, callback) {
  var params = new URLSearchParams();
  params.append('patientId', patientId);
  params.append('changedServices', JSON.stringify(changedServices));

  return {
    type: UPDATE_SERVICES_FOR_PATIENT,
    payload: axios.post('/services/' + patientId + '/update', params)
                  .then(response => {
                    typeof callback === 'function' && callback(); // optional callback for chaining actions
                    return response.data
                  })
  }
}

export function fetchEnabledServicesForPatient(patientId) {
  return {
    type: FETCH_ENABLED_SERVICES_FOR_PATIENT,
    payload: axios.get('/services/' + patientId)
                  .then(response => {
                    return {
                      patientId,
                      data: response.data
                    }
                  })
  }
}

export function openPatientAnalyticsModal(patientId) {
  return {
    type: OPEN_PATIENT_ANALYTICS_MODAL,
    patientId
  };
}

export function closePatientAnalyticsModal(patientId) {
  return {
    type: CLOSE_PATIENT_ANALYTICS_MODAL,
    patientId
  };
}

export function openPatientModal(patientId) {
  return {
    type: OPEN_PATIENT_MODAL,
    patientId
  };
}

export function closePatientModal(patientId) {
  return {
    type: CLOSE_PATIENT_MODAL,
    patientId
  };
}

export function fetchPatients() {
  return {
    type: FETCH_PATIENTS,
    payload : axios.get('/patients')
                   .then(response => {
                     return response.data
                   })
  }
}

export function fetchServices() {
  return {
    type: FETCH_SERVICES,
    payload : axios.post('/services/refresh')
                   .then(response => {
                     return axios.get('/services')
                   })
                   .then(response => {
                     return response.data;
                   })
  }
}

export function toggleSelectService(serviceName) {
    return { type: TOGGLE_SERVICE, serviceName };
}

export function startServices(dispatch, servicesMap) {
  var params = new URLSearchParams();
  params.append('services', JSON.stringify(Object.keys(servicesMap)));
  axios.post('/start', params)
    .then(response => {
      dispatch({
        type: START_SERVICES_FINISHED,
        services_status : 'started'
      })
    })
    .catch(err => {
      dispatch({
        type: START_SERVICES_ERROR,
        services_status : 'error',
        error : err
      })
    })

  return {
    type: START_SERVICES,
    services_status : 'starting'
  };
}

export function stopServices(dispatch, servicesMap) {
  var params = new URLSearchParams();
  params.append('services', JSON.stringify(Object.keys(servicesMap)));
  axios.post('/stop', params)
    .then(response => {
      dispatch({
        type: STOP_SERVICES_FINISHED,
        services_status : 'stopped'
      })
    })
    .catch(err => {
      dispatch({
        type: STOP_SERVICES_ERROR,
        services_status : 'error',
        error : err
      })
    })

  return {
    type: STOP_SERVICES,
    services_status : 'stopping'
  };
}
