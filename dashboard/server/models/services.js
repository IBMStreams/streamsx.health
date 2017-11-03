var fs = require('fs-extra')
var st = require('./streamtool')
var async = require('async')

const SERVICES_FILE = 'services/services.json'
const JOB_GROUP = "streamsx.health"

function Services() {
  this.services = JSON.parse(fs.readFileSync(SERVICES_FILE, 'utf8'))
  var i = this.services.services.length
  while(i--) {
    var svc = this.services.services[i];
    if(svc.disabled) {
      this.services.services.splice(i, 1)
    }
  }
}

Services.prototype.getAllServices = function() {
  return this.services['services']
}

Services.prototype.getAdapterServices = function() {
	return this.getServicesByType('adapter');
}

Services.prototype.getAggregatorServices = function() {
	return this.getServicesByType('aggregator');
}

Services.prototype.getAppServices = function() {
	return this.getServicesByType('app');
}

Services.prototype.getServicesByType = function(serviceType) {
	return this.services['services'].filter(function (svc) {
		return svc.type === serviceType;
	});
}

Services.prototype.getServiceByName = function(serviceName) {
	var arr = this.services['services'].filter(function(svc) {
		return svc.name === serviceName;
	})

	if(arr.length < 1)
		throw "Unable to find service: " + serviceName;

	return arr[0];
}

Services.prototype.getServiceNames = function() {
  var names = [];
  var svcs = this.services['services'];
  for(var idx in svcs) {
      names.push(svcs[idx].name);
  }

  return names;
}

Services.prototype.getServiceTypeNodeName = function(type) {
        if(type === 'adapter')
                return 'adapters';
        else if(type === 'aggregator')
                return 'aggregators';
        else if(type === 'app')
                return 'apps';
}


Services.prototype.refreshServiceState = function() {
  var self = this;
  var serviceObjs = this.services['services'];
	async.each(serviceObjs, function(service, servicesCallback) {
		var serviceName = service['name'];

    st.jobStatus(serviceName, JOB_GROUP, (status) => {
      //console.log(status);
      service['status'] = status
      // self.serviceState[serviceName].status = status;
      servicesCallback()
    })
	},
	function(err) {
		// all services have finished being processed
		//console.log('SERVICE STATE: ' + __pretty_print(serviceState));
	});
}

var _services = new Services()
module.exports = _services
