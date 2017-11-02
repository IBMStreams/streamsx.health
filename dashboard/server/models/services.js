var fs = require('fs-extra')
var st = require('./streamtool')

const SERVICES_FILE = 'services/services.json'

function Services() {
  this.serviceState = {}
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

Services.prototype.refreshServiceState = function() {
  var serviceObjs = this.services['services'];
	async.eachSeries(serviceObjs, function(service, servicesCallback) {
		var serviceName = service['name'];
		var serviceType = this.getServiceTypeNodeName(service['type']);
		//console.log(pretty_print(service));

    // add the service object to 'serviceState' variable, if it doesn't exist
    if(typeof this.serviceState[serviceName] === 'undefined') {
      this.serviceState[serviceName] = {};
    }

		async.waterfall([
        // get the status of the services from the backend server
		    function(wfCallback) {
          st.jobStatus(serviceName, JOB_GROUP, (status) => {
            //console.log(status);
            this.serviceState[serviceName].status = status;
            wfCallback();
          })
		    },
        function(wfCallback) {
          this.serviceState[serviceName].type = service['type'];
          wfCallback();
        }
		],
		function(err) {
			// finished retrieving info for this service
			servicesCallback();
      //console.log('SERVICES STATE: ' + __pretty_print(serviceState))
		})
	},
	function(err) {
		// all services have finished being processed
		//console.log('SERVICE STATE: ' + __pretty_print(serviceState));
	});
}

var _services = new Services()
module.exports = _services
