import { TOGGLE_SERVICE } from '../actions/actions'
import { FETCH_SERVICES_PENDING, FETCH_SERVICES_FULFILLED, FETCH_SERVICES_REJECTED } from '../actions/actions'
import { START_SERVICES, STOP_SERVICES } from '../actions/actions'
import { FETCH_PATIENTS_PENDING, FETCH_PATIENTS_FULFILLED, FETCH_PATIENTS_REJECTED } from '../actions/actions'
import { OPEN_PATIENT_MODAL, CLOSE_PATIENT_MODAL } from '../actions/actions'
import { OPEN_PATIENT_ANALYTICS_MODAL, CLOSE_PATIENT_ANALYTICS_MODAL } from '../actions/actions'
import { UPDATE_SERVICES_FOR_PATIENT_PENDING, UPDATE_SERVICES_FOR_PATIENT_FULFILLED, UPDATE_SERVICES_FOR_PATIENT_REJECTED } from '../actions/actions'
import { FETCH_ENABLED_SERVICES_FOR_PATIENT_PENDING, FETCH_ENABLED_SERVICES_FOR_PATIENT_FULFILLED, FETCH_ENABLED_SERVICES_FOR_PATIENT_REJECTED } from '../actions/actions'
import { UPDATE_ALERTS_PENDING, UPDATE_ALERTS_FULFILLED, UPDATE_ALERTS_REJECTED } from '../actions/actions'
import { OPEN_EDIT_ALERTS_MODAL, CLOSE_EDIT_ALERTS_MODAL } from '../actions/actions'
import { FETCH_ALERT_RULES_FULFILLED, FETCH_ALERT_RULES_PENDING, FETCH_ALERT_RULES_REJECTED } from '../actions/actions'
import { ALERT_RULE_SELECTED_FOR_EDITING } from '../actions/actions'
import { ADD_ALERT_RULE_PENDING, ADD_ALERT_RULE_FULFILLED, ADD_ALERT_RULE_REJECTED } from '../actions/actions'
import { FETCH_PATIENT_VITALS_DATA_PENDING, FETCH_PATIENT_VITALS_DATA_FULFILLED, FETCH_PATIENT_VITALS_DATA_REJECTED } from '../actions/actions'
import { FETCH_PATIENT_ACTIVE_ALERTS_PENDING, FETCH_PATIENT_ACTIVE_ALERTS_FULFILLED, FETCH_PATIENT_ACTIVE_ALERTS_REJECTED } from '../actions/actions'
import { UPDATE_DISPLAYED_VITALS } from '../actions/actions'
import { FETCH_AVAILABLE_PATIENT_VITALS_PENDING, FETCH_AVAILABLE_PATIENT_VITALS_FULFILLED, FETCH_AVAILABLE_PATIENT_VITALS_REJECTED } from '../actions/actions'
import { SET_PATIENT_CARD_SIZE } from '../actions/actions'
import { SET_MODAL_DISPLAY_TYPE } from '../actions/actions'
import { SET_PATIENT_CARDS_FILTER_TYPES } from '../actions/actions'

const initialState = {
  selectedServices: [],
  fetchingServices: false,
  servicesMap: {},
  error: undefined,
  fetchingPatients: false,
  patients: [],
  modalPatientId: "",
  fetchingPatientServices: false,
  analyticsModalPatientId: "",
  patientServices: {},
  isEditAlertsModalOpen : false,
  alertRulesFilter : "",
  alertsForFilter : [],
  isListingAlertRulesPending : false,
  isAddingAlertRule : false,
  isFetchingPatientVitalsData : false,
  updatedVitalNames : [],
  vitalsData : {},
  activeAlerts : {},
  isFetchingPatientActiveAlerts : false,
  displayedVitals : {},
  availableVitals : {},
  isFetchingAvailableVitals : false,
  patientCardSize : "large",
  modalDisplayType : "general",
  patientCardsFilterTypes : []
};

function handleActions(state = initialState, action) {
  switch(action.type) {
    case TOGGLE_SERVICE:
      var newState = {...state};
      var idx = state.selectedServices.indexOf(action.serviceName);
      if(idx > -1) {
        newState.selectedServices.splice(idx, 1);
      } else {
        newState.selectedServices.push(action.serviceName);
      }
      return newState;
    case FETCH_SERVICES_PENDING:
      return {...state, fetchingServices: true}
    case FETCH_SERVICES_FULFILLED:
      return {...state, fetchingServices: false, servicesMap : action.payload}
    case FETCH_SERVICES_REJECTED:
      return {...state, fetchingServices: false, error: action.payload}
    case FETCH_PATIENTS_PENDING:
      return {...state, fetchingPatients: true}
    case FETCH_PATIENTS_FULFILLED:
      return {...state, fetchingPatients: false, patients : Object.keys(action.payload), patientInfo : action.payload}
    case FETCH_PATIENTS_REJECTED:
      return {...state, fetchingPatients: false, error: action.payload}
    case START_SERVICES:
      return {...state}
    case STOP_SERVICES:
      return {...state}
    case OPEN_PATIENT_MODAL:
      return {...state, modalPatientId: action.patientId}
    case CLOSE_PATIENT_MODAL:
      return {...state, modalPatientId: "", patientServices: {}, vitalsData: {}}
    case OPEN_PATIENT_ANALYTICS_MODAL:
      return {...state, analyticsModalPatientId: action.patientId}
    case FETCH_ENABLED_SERVICES_FOR_PATIENT_PENDING:
      return {...state, fetchingPatientServices: true}
    case FETCH_ENABLED_SERVICES_FOR_PATIENT_FULFILLED:
      return {...state, fetchingPatientServices: false, patientServices: action.payload.data}
    case FETCH_ENABLED_SERVICES_FOR_PATIENT_REJECTED:
      return {...state, fetchingPatientServices: false, error: action.payload}
    case CLOSE_PATIENT_ANALYTICS_MODAL:
      return {...state, analyticsModalPatientId: ""}
    case UPDATE_SERVICES_FOR_PATIENT_PENDING:
      return {...state, updatingPatientServices: true}
    case UPDATE_SERVICES_FOR_PATIENT_FULFILLED:
      return {...state, updatingPatientServices: false}
    case UPDATE_SERVICES_FOR_PATIENT_REJECTED:
      return {...state, updatePatientServices: false, error: action.payload}
    case UPDATE_ALERTS_PENDING:
      return {...state, updatingAlerts : true}
    case UPDATE_ALERTS_FULFILLED:
      return {...state, updatingAlerts: false, patientAlerts: action.payload.data}
    case UPDATE_ALERTS_REJECTED:
      return {...state, updatingAlerts: false, error: action.payload}
    case OPEN_EDIT_ALERTS_MODAL:
      return {...state, isEditAlertsModalOpen : true}
    case CLOSE_EDIT_ALERTS_MODAL:
      return {...state, isEditAlertsModalOpen : false}
    case FETCH_ALERT_RULES_PENDING:
      return {...state, isListingAlertRulesPending: true}
    case FETCH_ALERT_RULES_FULFILLED:
      return {...state, alertsForFilter : action.payload.data.alertRules, alertRulesFilter : action.payload.alertRulesFilter, isListingAlertRulesPending: false }
    case FETCH_ALERT_RULES_REJECTED:
      return {...state, alertRulesFilter : action.payload.alertRulesFilter, error : action.payload, isListingAlertRulesPending: false}
    case ALERT_RULE_SELECTED_FOR_EDITING:
      return {...state, alertRuleForEditing : JSON.parse(action.alertRuleForEditing)}
    case ADD_ALERT_RULE_PENDING:
      return {...state, isAddingAlertRule: true}
    case ADD_ALERT_RULE_FULFILLED:
      return {...state, isAddingAlertRule: false}
    case ADD_ALERT_RULE_REJECTED:
      return {...state, isAddingAlertRule: false, error: action.payload}
    case FETCH_PATIENT_VITALS_DATA_PENDING:
      return {...state, isFetchingPatientVitalsData: true}
    case FETCH_PATIENT_VITALS_DATA_FULFILLED:
      {
        var vitalsData = {...state.vitalsData}
        for(var idx in action.payload.vitalNames) {
          var name = action.payload.vitalNames[idx]
          vitalsData[name] = action.payload.data[name]
        }
        return {...state, vitalsData, updatedVitalNames : action.payload.vitalNames}
      }
    case FETCH_PATIENT_VITALS_DATA_REJECTED:
      return {...state, isFetchingPatientVitalsData : false, error : action.payload}
    case FETCH_PATIENT_ACTIVE_ALERTS_PENDING:
      return {...state, isFetchingPatientActiveAlerts: true}
    case FETCH_PATIENT_ACTIVE_ALERTS_FULFILLED:
      return {...state, isFetchingPatientActiveAlerts : false, activeAlerts : action.payload}
    case FETCH_PATIENT_ACTIVE_ALERTS_REJECTED:
      return {...state, isFetchingPatientActiveAlerts: false, error: action.payload}
    case UPDATE_DISPLAYED_VITALS:
      {
        var displayedVitals = {...state.displayedVitals}
        var patientId = action.patientId;
        displayedVitals[patientId] = action.vitalNames
        return {...state, displayedVitals : displayedVitals}
      }
    case FETCH_AVAILABLE_PATIENT_VITALS_PENDING:
      return {...state, isFetchingAvailableVitals: true}
    case FETCH_AVAILABLE_PATIENT_VITALS_FULFILLED:
      {
        var availableVitals = {...state.availableVitals}
        availableVitals[action.payload.patientId] = action.payload.availableVitals
        return {...state, isFetchingAvailableVitals: false, availableVitals}
      }
    case FETCH_AVAILABLE_PATIENT_VITALS_REJECTED:
      return {...state, isFetchingAvailableVitals: false, error: action.payload}
    case SET_PATIENT_CARD_SIZE:
      return {...state, patientCardSize : action.cardSize}
    case SET_MODAL_DISPLAY_TYPE:
      return {...state, modalDisplayType : action.modalDisplayType}
    case SET_PATIENT_CARDS_FILTER_TYPES:
      return {...state, patientCardsFilterTypes : action.patientCardsFilterTypes}
    default:
      console.log("Not Yet Implemented: " + action.type)
      return state;
  }
}

export default handleActions;
