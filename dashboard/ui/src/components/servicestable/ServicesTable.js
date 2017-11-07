import React from 'react'
import { string, func, object, array } from 'prop-types'

const tableStyle = {
  marginTop: '20px',
  width: '100%'
};

const containerStyle = {
  marginTop: '30px',
  width: '40%'
};

const rowStyle = {
  marginTop: '20px'
};

const headerRowStyle = {
  fontWeight: 'bold',
  background: 'white'
};

const rowCellStyle = {
  fontSize: '22px',
  backgroundColor: 'lightgrey'
}

class ServicesTable extends React.Component {

  static propTypes = {
    servicesMap : object,
    selectedServices : array,
    onServiceClick : func
  };

  getSection(typeName) {
    var svcs = {};
    for (var idx in this.props.servicesMap) {
      var svc = this.props.servicesMap[idx]
      if (svc.type === typeName) {
        svcs[idx] = svc;
      }
    }

    return <ServicesTableSection key={typeName} title={this.capitalizeFirstLetter(typeName)} services={svcs} selectedServices={this.props.selectedServices} onServiceClick={this.props.onServiceClick}/>
  }

  getSections() {
    const types = ['adapter', 'app'];
    let arr = [];
    for (var t in types) {
      arr.push(this.getSection(types[t]));
    }
    return arr;
  }

  render() {
    return (
      <div style={containerStyle} className="container">
        <div className="row" style={rowStyle}>
          <div className="col">
            <table style={tableStyle} className="table table-bordered table-sm text-center">
              {this.getSections()}
            </table>
          </div>
        </div>
      </div>
    )
  }

  capitalizeFirstLetter(string) {
    return string.charAt(0).toUpperCase() + string.slice(1);
  }
}

/*
 * ServicesTableSection
 */
class ServicesTableSection extends React.Component {
  createServiceRow(svcName, svc) {
    var isChecked = this.props.selectedServices.indexOf(svcName) > -1
    ? true
    : false;

    return (
      <ServicesTableServiceRow key={svcName} name={svcName} isChecked={isChecked} topics={svc.outputTopics} status={svc.status} onClick={() => this.props.onServiceClick(svcName)}/>
    );
  }

  createServicesRows(services) {
    let arr = [];
    for (var svc in services) {
      arr.push(this.createServiceRow(svc, services[svc]));
    }
    return arr;
  }

  render() {
    return (
      <tbody>
        <ServicesTableTitleRow title={this.props.title}/>
        <ServicesTableHeaderRow/>
        {this.createServicesRows(this.props.services)}
      </tbody>
    )
  };
}

/*
 * ServicesTableHeaderRow
 */
var ServicesTableHeaderRow = (props) => {
  return (
    <tr style={headerRowStyle}>
      <td>Name</td>
      <td>Topics</td>
      <td>Status</td>
    </tr>
  )
};

/*
 * ServicesTableTitleRow
 */
var ServicesTableTitleRow = (props) => {
  return (
    <tr>
      <td style={rowCellStyle} colSpan='4'>{props.title}</td>
    </tr>
  )
};

/*
 * ServicesTableServiceRow
 */
class ServicesTableServiceRow extends React.Component {
  static propTypes = {
    onClick : func.isRequired,
    checked : string,
    name : string.isRequired,
    topics : array,
    status : string.isRequired
  }

  constructor(props) {
    super(props)
    this.state = {
      isChecked : false
    }
  }

  handleCheck(evt, checked) {
    console.log(checked)
    this.setState({isChecked : !this.state.isChecked})
  }

  render() {
    var props = this.props;
    var rowClass = 'table-warning'
    if(props.status === 'stopped')
      rowClass = 'table-danger'
    else
      rowClass = 'table-success'

    return (
      <tr className={rowClass}>
        <td className="serviceRowName">{props.name}</td>
        <td className="serviceRowTopics">{props.topics}</td>
        <td className="serviceRowStatus">{props.status}</td>
      </tr>
    )
  }
}

export default ServicesTable;
