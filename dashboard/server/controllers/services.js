var express = require('express')
var router = express.Router()
var async = require('async')
var client = require('../models/redis_backend')
var services = require('../models/services')

router.get('/', function(req, res) {
  var serviceType = req.query.type || undefined;

  var svcs = {};
  if(typeof serviceType === 'undefined') {
    svcs = services.getAllServices()
  } else {
    svcs = services.getServicesByType(serviceType);
  }

  var svcsObj = {};
  for(var idx in svcs) {
    var svc = svcs[idx]
    svcsObj[svc.name] = svc
  }

  res.json(svcsObj);
})

router.all('/refresh', function(req, res) {
  services.refreshServiceState()
  var response = {
    status : "Services refreshed"
  }
  res.json(response)
})

router.get('/:patientId', function(req, res) {
  var patientId = req.params['patientId']

  var svcs = services.getServicesByType('app'); // only app services contain patient info
  console.log(svcs);

  async.map(svcs, function(service, callback) {
    var serviceName = service['name'];
    client.getPatientsForService(serviceName, (reply) => {
        var isEnabled  = reply.indexOf(patientId) > -1;
        var data = {
          serviceName: serviceName,
          isEnabledForPatient: isEnabled
        };
        callback(null, data);
    })
  }, function(err, results) {
    // all services have been checked
    var resObj = {};
    for(var idx in results) {
      var svcInfo = results[idx];
      resObj[svcInfo.serviceName] = {
        isEnabledForPatient: svcInfo.isEnabledForPatient
      };
    }

    res.json(resObj);
  })
})

router.post('/:patientId/update', function(req, res) {
  var patientId = req.params['patientId']
  var changedServices = JSON.parse(req.body.changedServices);

  async.map(Object.keys(changedServices), function(serviceName, callback) {
    var isEnabledForPatient = changedServices[serviceName].isEnabledForPatient;
    if(isEnabledForPatient) {
      client.addPatientToService(patientId, serviceName, (reply) => {
        var result = {
          patientId: patientId,
          serviceName: serviceName,
          status: reply
        }
        callback(null, result);
      });
    } else {
      client.removePatientFromService(patientId, serviceName, (reply) => {
        var result = {
          patientId: patientId,
          serviceName: serviceName,
          status: reply
        }
        callback(null, result);
      })
    }
  }, function(err, results) {
    console.log("results", results);
    res.json(results)
  })
})

router.all('/stop', function(req, res) {
  services.stopService(function(response) {
    res.send(response);
  })
})

router.all('/start', function(req, res) {
  var serviceNames = req.method === 'POST' ? JSON.parse(req.body.services) : services.getServiceNames();

  async.series([
    // launch all services
    function(callback) {
      async.eachSeries(serviceNames, function(serviceName, launchServiceCallback) {
        async.series([
          // check if service is started
          // if started, error is returned and no attempt will be made to start the service
          function(cb) {
              services.checkIfServiceStarted(serviceName, cb);
          },
          // start service if not already started
          function(cb) {
            services.startService(serviceName, cb);
          }
        ], function(err, results) {
          launchServiceCallback(null, results);
        })
      }, function(err, results) {
        callback();
      })
    },
    // launch bridge adapters
    function(callback) {
      console.log("Launching bridge services...");
      services.launchBridgeServices(callback);
    }
  ], function(err, results) {
    console.log("All services launched!");
    res.send('Services started')
  });

})

module.exports = router
