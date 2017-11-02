const async = require('async')
const redis = require('redis')
const util = require('util');
const EventEmitter = require('events')

const KEY_PREFIX = 'com.ibm.streamsx.health:';
const SERVICES_KEY_PREFIX = KEY_PREFIX + 'services:';
const PATIENTS_KEY = KEY_PREFIX + 'patients'
const ALERTS_KEY_PREFIX = KEY_PREFIX + 'alerts:';
const ALERT_RULES_KEY = KEY_PREFIX + 'alert_rules'
const DATA_KEY_PREFIX = KEY_PREFIX + "data:"
const ACTIVE_ALERTS_KEY_PREFIX = KEY_PREFIX + "alerts:active:"

function RedisBackendClient() {

}
util.inherits(RedisBackendClient, EventEmitter);

RedisBackendClient.prototype.connect = function(url) {
  EventEmitter.call(this);
  var self = this;

  this.redisClient = redis.createClient(url);
  this.redisClient.on("error", function(err) {
    console.log("Error " + err);
  });
  this.redisClient.on('ready', function() {
    self.emit('ready');
  });
}

RedisBackendClient.prototype.getActiveAlerts = function(callback) {
  var self = this;

  // first retrieve list of patients, then look for alerts for those patients
  this.getPatients((patients) => {
    async.map(patients, function(patientName, cb) {
      self.redisClient.hgetall(ACTIVE_ALERTS_KEY_PREFIX + patientName, (err, reply) => {
        var alerts = {}

        if(typeof reply !== 'undefined') {
          for(var idx in reply) {
            alerts[idx] = JSON.parse(reply[idx])
          }
        }
        cb(null, alerts);
      });
    }, function(err, results) {
      var data = {}
      for(var idx in patients) {
        data[patients[idx]] = results[idx];
      }
      callback(data);
    })
  })
}

RedisBackendClient.prototype.getData = function(patientId, vitalNames, start, end, callback) {
  var self = this;

  async.map(vitalNames, function(vitalName, cb) {
    var key = DATA_KEY_PREFIX + patientId + ":" + vitalName;
    self.redisClient.zrangebyscore(key, start, end, function(err, reply) {
      // console.log('key=', key, 'start=', start, 'end=', end)
      if(typeof reply === 'undefined') {
        cb(null, [])
      } else {
        var arr = []
        reply.forEach((v) => {
          arr.push(JSON.parse(v))
        })
        cb(null, arr)
      }
    })
  }, function(err, results) {
    var data = {}
    for(var idx in vitalNames) {
      data[vitalNames[idx]] = results[idx];
    }
    callback(data);
  })
}

RedisBackendClient.prototype.getServiceState = function(serviceName, callback) {
  this.redisClient.get(SERVICES_KEY_PREFIX + serviceName + ':state', function(err, reply) {
    callback(reply);
  });
}

RedisBackendClient.prototype.getServiceStatus = function(serviceName, callback) {
  this.getServiceState(serviceName, function(reply) {
    if(reply === null) {
      callback('stopped');
    } else {
      callback(JSON.parse(reply).status);
    }
  });
};

RedisBackendClient.prototype.getServiceJobId = function(serviceName, callback) {
  this.getServiceState(serviceName, function(reply) {
    if(reply === null) {
      callback('-1');
    } else {
      var jsonReply = JSON.parse(reply);
      if(jsonReply.status === 'stopped') {
        callback('-1');
      } else {
          callback(jsonReply.jobId);
      }
    }
  });
};

RedisBackendClient.prototype.getServiceOutputTopics = function(serviceName, callback) {
  this.redisClient.smembers(SERVICES_KEY_PREFIX + serviceName + ':topics', function(err, reply) {
    if(reply.length == 1 && reply[0] === "") {
      callback([]);
    } else {
      callback(reply);
    }
  });
};

RedisBackendClient.prototype.getPatients = function(callback) {
  this.redisClient.hkeys(PATIENTS_KEY, function(err, reply) {
    if(typeof reply === 'undefined' || (reply.length === 1 && reply[0] === "")) {
      callback([]);
    } else {
      callback(reply);
    }
  });
};

RedisBackendClient.prototype.getPatientsInfo = function(callback) {
  this.redisClient.hgetall(PATIENTS_KEY, function(err, reply) {
    if(typeof reply === 'undefined' || reply === null || (reply.length === 1 && reply[0] === "")) {
      callback([]);
    } else {
      callback(reply)
    }
  })
}

RedisBackendClient.prototype.getAlertsForPatient = function(patientId, start, stop, callback) {
  this.redisClient.zrangebyscore(ALERTS_KEY_PREFIX + patientId, start, stop, function(err, reply) {
    if(typeof reply === 'undefined' || (reply.length === 1 && reply[0] === "")) {
      callback([]);
    } else {
      callback(reply);
    }
  });
}

RedisBackendClient.prototype.getPatientsForService = function(serviceName, callback) {
  this.redisClient.smembers(SERVICES_KEY_PREFIX + serviceName + ':patients', function(err, reply) {
    //console.log("Checking service: " + serviceName);
    if(typeof reply === 'undefined' || (reply.length === 1 && reply[0] === "")) {
      callback([]);
    } else {
      console.log("reply:", reply)
      callback(reply);
    }
  })
}

RedisBackendClient.prototype.addPatientToService = function(patientId, serviceName, callback) {
  this.redisClient.sadd(SERVICES_KEY_PREFIX + serviceName + ":patients", patientId, function(err, reply) {
      if(typeof reply === 'undefined' || (reply.length === 1 && reply[0] === 1)) {
        callback("success");
      } else {
        callback("failed");
      }
  })
}

RedisBackendClient.prototype.removePatientFromService = function(patientId, serviceName, callback) {
  this.redisClient.srem(SERVICES_KEY_PREFIX + serviceName + ":patients", patientId, function(err, reply) {
      if(reply.length === 1 && reply[0] === 1) {
        callback("success");
      } else {
        callback("failed");
      }
  })
}

RedisBackendClient.prototype.getAlertsList = function(callback) {
  this.redisClient.smembers(ALERT_RULES_KEY, function(err, reply) {
    if(reply.length === 1 && reply[0] === "") {
      callback([]);
    } else {
      callback(reply);
    }
  })
}

RedisBackendClient.prototype.removeAlertRule = function(oldAlertRule, callback) {
  this.redisClient.srem(ALERT_RULES_KEY, oldAlertRule, function(err, reply) {
    if(reply.length === 1 && reply[0] === "") {
      callback([]);
    } else {
      callback(reply);
    }
  })
}

RedisBackendClient.prototype.addAlertRule = function(newAlertRule, callback) {
  this.redisClient.sadd(ALERT_RULES_KEY, newAlertRule, function(err, reply) {
    if(reply.length === 1 && reply[0] === "") {
      callback([]);
    } else {
      callback(reply);
    }
  })
}

RedisBackendClient.prototype.getAvailableVitals = function(patientId, callback) {
  this.redisClient.smembers(DATA_KEY_PREFIX + patientId + ":data_type_index", function(err, reply) {
    if(typeof reply === 'undefined' || (reply.length === 1 && reply[0] === "")) {
      callback([])
    } else {
      callback(reply)
    }
  });
}

var Redis = new RedisBackendClient()
module.exports = Redis;
