var fs = require('fs-extra')
var st = require('./streamtool')
var async = require('async')

const SERVICES_FILE = 'services/services.json'
const JOB_GROUP = "streamsx.health"
const BRIDGE_SERVICE_OUTPUT_TOPIC = "streamsx.health.aggregated.observations";
const BRIDGE_SERVICE_SAB_PATH = "services/AggregatorService/com.ibm.streamsx.health.aggregator.AggregatorService.sab";

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
	async.eachSeries(serviceObjs, function(service, servicesCallback) {
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

Services.prototype.checkIfServiceStarted = function(serviceName, callback) {
  console.log("Check if " + serviceName + " is started...")
  st.jobStatus(serviceName, JOB_GROUP, function(status) {
    if(status === 'running') {
      var msg = 'Service ' + serviceName + ' already started!'
      console.log(msg);
      callback(msg, status);
    } else {
      callback(null, status);
    }
  })
}

Services.prototype.startService = function(serviceName, callback) {
  console.log("Starting service: " + serviceName);
  var service = this.getServiceByName(serviceName);
  var serviceType = service.type;

  console.log('Submitting ' + serviceName);
  if(service.launch_details.type === 'sab') {
    console.log("SERVICE:", service);
    var sabPath = 'services/' + serviceName + '/' + service.launch_details.details.sab_filename
    var params = {};
    for(var idx in service.parameters) {
      var param = service.parameters[idx];
      params[param.name] = param.default;
    }
    console.log("PARAMS:", params);

    st.submitjob(sabPath, params, serviceName, JOB_GROUP, function(response) {
      console.log(response);
      callback(null, response);
    })
  }
}

Services.prototype.stopService = function(callback) {
  // stop all services
  console.log("Stopping services in job group: " + JOB_GROUP);
  st.stopServicesInJobGroup(JOB_GROUP, function(response) {
    callback("Services stopped");
  });
}

Services.prototype._launchBridgeService = function(inputTopic, serviceName, callback) {
  var sabPath = BRIDGE_SERVICE_SAB_PATH
  var params = {
    "inputTopic" : inputTopic,
    "outputTopic" : BRIDGE_SERVICE_OUTPUT_TOPIC
  };
  console.log("PARAMS:", params);

  st.submitjob(sabPath, params, serviceName, JOB_GROUP, function(response) {
    console.log(response);
    callback(null, response);
  })
}

Services.prototype.launchBridgeServices = function(callback) {
  var self = this;

  var adapterServices = self.getAdapterServices();
  async.eachSeries(adapterServices, function(adapterService, launchServiceCallback) {
    var serviceName = adapterService.name + "_Bridge";
    async.series([
      // check if service is started
      function(cb) {
        self.checkIfServiceStarted(serviceName, cb);
      },
      // start service
      function(cb) {
        if(adapterService.hasOwnProperty("bridgeTopics")) {
          var topic = adapterService.bridgeTopics[0];
          self._launchBridgeService(topic, serviceName, cb);
        } else {
          st.getOutputTopics(adapterService.name, JOB_GROUP, (outputTopics) => {
            console.log("Output topics for service ", adapterService.name, ": ", outputTopics);
            // refreshServiceState();
            if(outputTopics.length === 0) {

              cb("No output topics found for service: " + adapterService.name, null);
              return;
            }
            var serviceOutputTopic = outputTopics[0]; // for now, just assign to the first output topic
            self._launchBridgeService(serviceOutputTopic, serviceName, cb);
          })
        }
      }
    ], function(err, result) {
      launchServiceCallback(err, result);
    });
  }, function(err, result) {
    callback(err, result);
  })

}

var _services = new Services()
module.exports = _services
