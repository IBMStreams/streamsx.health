import React from 'react'
import { string } from 'prop-types'
import { connect } from 'react-redux'
import { setPatientCardSize, setModalDisplayType, setPatientCardsFilterTypes } from '../../actions/actions'
import Toggle from 'react-toggle'

const mapStateToProps = state => {
  return {
    patientCardSize : state.patientCardSize,
    panelType : state.panelType,
    modalDisplayType : state.modalDisplayType,
    patientCardsFilterTypes : state.patientCardsFilterTypes
  }
};

const mapDispatchToProps = dispatch => {
  return {
    setPatientCardSize : (cardSize) => {
      dispatch(setPatientCardSize(cardSize))
    },
    setModalDisplayType : (modalDisplayType) => {
      dispatch(setModalDisplayType(modalDisplayType))
    },
    setPatientCardsFilterTypes : (patientCardsFilterTypes) => {
      dispatch(setPatientCardsFilterTypes(patientCardsFilterTypes))
    }
  }
}

const navStyle = {
  backgroundColor : "#9398b6",
  "color" : "#ffffff"
}

class NavBar extends React.Component {
  static propTypes = {
    title : string.isRequired,
    activeLinkName : string.isRequired
  }

  constructor(props) {
    super(props)

    this.getNavItem = this.getNavItem.bind(this);
    this.handleChangePatientCardSize = this.handleChangePatientCardSize.bind(this)
    this.handleFilterCards = this.handleFilterCards.bind(this)
  }

  handleChangePatientCardSize() {
    var currentSize = this.props.patientCardSize;
    if(currentSize === "small") {
      this.props.setPatientCardSize("large")
    } else {
      this.props.setPatientCardSize("small")
    }
  }

  handleFilterCards() {
    var currentFilterTypes = this.props.patientCardsFilterTypes;
    if(currentFilterTypes.length > 0) {
      this.props.setPatientCardsFilterTypes([])
    } else {
      this.props.setPatientCardsFilterTypes(["alertsOnly"])
    }
  }

  getNavItem(navItemName, url) {
    var navItemClass = (navItemName === this.props.activeLinkName) ? "nav-item active" : "nav-item";
    return (
      <li className={navItemClass}>
        <a className="nav-link" href={url}>{navItemName}</a>
      </li>
    )
  }

  render() {
    var cardsFilterStyle = {color : "white"}
    if(this.props.patientCardsFilterTypes.indexOf("alertsOnly") !== -1) {
      cardsFilterStyle = {color : "orange"}
    }

    return (
      <nav className="navbar navbar-toggleable-md navbar-inverse" style={navStyle}>
    		<img src="./images/monitoring_logo.png" width="30" height="30" className="d-inline-block align-top" alt="" />
    		<div style={{"width" : "10px"}}></div>
    		<a className="navbar-brand" style={{"fontWeight" : "bold"}}>{this.props.title}</a>
    		<div className="">
    			<ul className="nav navbar-nav">
            {this.getNavItem("Home", "/")}
            {this.getNavItem("Admin", "/admin")}
            {this.getNavItem("Events", "/events")}
    			</ul>
    		</div>
        <div className="text-right" style={{width : "100%"}}>
          <a className="btn btn-default btn-sm" style={cardsFilterStyle} onClick={this.handleFilterCards} href="#"><i className="fa fa-medkit fa-2x"></i></a>
          <a className="btn btn-default btn-sm" style={{color : "white"}} onClick={this.handleChangePatientCardSize} href="#"><i className="fa fa-th-large fa-2x"></i></a>
        </div>
    	</nav>
    );
  }
}

export default connect(mapStateToProps, mapDispatchToProps)(NavBar);
