const async = require('async')
const util = require('util');
const scp = require('scp2')
const Rsync = require('rsync')
const ssh = require('ssh2')
const path = require('path')
const axios = require('axios')

process.env.NODE_TLS_REJECT_UNAUTHORIZED = '0';

const EventEmitter = require('events')

const TEMP_DIR = '/tmp'

function Streamtool() {
  EventEmitter.call(this);
};
util.inherits(Streamtool, EventEmitter);

Streamtool.prototype.connect = function(params) {
  var self = this;
  this.conn = new ssh.Client();
  this.sshParams = params
  this.instanceName = params['instanceName']
  this.domainName = params['domainName']
  this.restURL = "https://" + this.sshParams.host + ":" + this.sshParams.apiPort + "/streams/rest";

  this.conn.on('error', function(err) {
    self.emit('error', err);
  })

  this.conn.on('ready', function() {
    self.emit('ready');
  }).connect({
    host: this.sshParams.host,
    port: this.sshParams.port,
    username : this.sshParams.username,
    password : this.sshParams.password
  })
}

Streamtool.prototype.mkjobgroup = function(jobGroup, callback) {
  this.conn.exec('streamtool mkjobgroup ' + jobGroup, function(err, stream) {
    stream.on('close', function(code, signal) {
      callback('success')
    }).on('data', function(data) {
      console.log('STDOUT: ' + data)
    }).stderr.on('data', function(data) {
      console.log('STDERR: ' + data)
    })
  });
}

Streamtool.prototype.getOutputTopics = function(jobName, jobGroup, callback) {
  var self = this;

  async.waterfall([
    // get exported streams output port urls
    function(cb) {
      var url = self.restURL + "/instances/" + self.instanceName + "/exportedstreams";
      //console.log("RESTURL: " + url);
      axios({
        method: 'get',
        url : url,
        auth : {
          username : self.sshParams.username,
          password : self.sshParams.password
        }
      }).then((response) => {
        var exportedStreams = response.data.exportedStreams
        var operatorOutputPortUrls = []
        for(var idx in exportedStreams) {
          var outputPortUrl = exportedStreams[idx].operatorOutputPort
          operatorOutputPortUrls.push(outputPortUrl);
        }

        cb(null, operatorOutputPortUrls);
      }).catch((err) => {
        if(err) {
          console.log("ERR [getOutputTopics]: " + err);
          cb(err, null);
        }
      })
    },

    // find topics
    function(operatorOutputPortUrls, cb) {
      //console.log("operatorOutputPortUrls: " + operatorOutputPortUrls);
      var topics = [];
      async.each(operatorOutputPortUrls, function(url, urlCallback) {
        axios({
          method : 'get',
          url : url,
          auth : {
            username : self.sshParams.username,
            password : self.sshParams.password
          }
        }).then((response) => {
            var exportProps = response.data.export.properties;
            for(var idx in exportProps) {
              var prop = exportProps[idx];
              if(prop.name === '__spl_topic') {
                topics.push.apply(topics, prop.values)
                break;
              }
            }
            urlCallback(null, topics)
        }).catch((err) => {
          urlCallback(err, null);
        })
      }, function(err) {
        if(err) {
          console.log("ERR: ", err)
          cb(err, null)
        } else {
          cb(null, topics);
        }
      })
    }
  ], function(err, outputTopics) {
    if(err) {
      console.log("ERR [getOutputTopics]: ", err);
    }
    callback(outputTopics)
  })
}

Streamtool.prototype.jobStatus = function(jobName, jobGroup, callback) {
  var self = this;
  var url = this.restURL + "/instances/" + this.instanceName + "/jobs"
  console.log("url=", url)
  var status = 'stopped';

  axios({
    method: 'get',
    url : url,
    auth : {
      username : self.sshParams.username,
      password : self.sshParams.password
    }
  }).then((response) => {
    var jobs = response.data.jobs
    for(var idx in jobs) {
      var job = jobs[idx];
      if(job.name === jobName && job.jobGroup === jobGroup) {
        status = job.status;
      }
    }
    return (status)
  }).then((status) => {
    callback(status);
  }).catch((err) => {
    if(err) {
      console.log("ERR [jobStatus]: " + err);
    }
  })
}

Streamtool.prototype.stopServicesInJobGroup = function(jobGroup, callback) {
  var self = this;

  var url = this.restURL + "/instances/" + this.instanceName + "/jobs"
  async.waterfall([
    // get job IDs for jobs in job group
    function(cb) {
      console.log("Looking for job IDs...");
      axios({
        method : 'get',
        url : url,
        auth : {
          username : self.sshParams.username,
          password : self.sshParams.password
        }
      }).then((response) => {
        var jobIds = [];
        var jobs = response.data.jobs;
        for(var idx in jobs) {
          var job = jobs[idx];
          if(job.jobGroup === jobGroup) {
            jobIds.push(job.id);
          }
        }

        console.log("Found job IDs: ", jobIds);
        cb(null, jobIds);
      }).catch((err) => {
        console.log("ERR [stopServicesInJobGroup]: ", err);
        cb(err, null);
      })
    },
    // cancel jobs
    function(jobIds, cb) {
      var cmd = 'streamtool canceljob ' + jobIds.join(',')
      console.log(cmd);
      self.conn.exec(cmd, function(err, stream) {
        stream.on('close', function(code, signal) {
          cb(null, "streamtool canceljob completed");
        }).on('data', function(data) {
          console.log('STDOUT: ' + data)
        }).stderr.on('data', function(data) {
          console.log('STDERR: ' + data)
          cb(data, null);
        })
      })
    }
  ], function(err, result) {
    if(err) {
      console.log("ERR [stopServicesInJobGroup]: " + err);
    }

    callback(result);
  })
}

Streamtool.prototype.submitjob = function(sabPath, params, jobName, jobGroup, callback) {
  var self = this;
  var remoteFilename = TEMP_DIR + '/' + path.basename(sabPath)

  async.series([
    function(cb) {
      console.log("Copying file " + sabPath + " to remote server...");
      var rsync = new Rsync()
        .shell('ssh')
        .flags('az')
        .set('update')
        .set('chmod', 'u+rwx')
        .set('progress')
        .source(sabPath)
        .destination(self.sshParams.username + '@' + self.sshParams.host + ':' + remoteFilename)

      rsync.execute(function(error, code, cmd) {
        console.log('[submitjob] Finished rsync! cmd=', cmd)
        if(error) {
          console.error('[submitJob] rsync error: ', error, ', code: ', code)
          cb(error, null)
        } else {
          cb(null, 'rysnc completed!')
        }
      })
    },

    function(cb) {
      console.log("Execute `streamtool submitjob " + remoteFilename + "`");

      var paramStr = "";
      for(var paramName in params) {
        paramStr += '-P ' + paramName + '=' + params[paramName] + ' ';
      }
      var cmd = 'streamtool submitjob --jobgroup ' + jobGroup + ' --jobname ' + jobName + ' ' + remoteFilename + ' ' + paramStr + ' -C tracing=off';
      self.conn.exec(cmd, function(err, stream) {
        stream.on('data', function(data) {
          console.log('STDOUT: ' + data);
        }).on('close', function(code, signal) {
          console.log('CLOSE: code=' + code + ', signal=' + signal)
          cb(null, "streamtool submitjob completed: cmd=" + cmd);
        }).stderr.on('data', function(data) {
          console.log('STDERR: ' + data)
        })
      });
    }
  ], function(err, results) {
    if(err) {
      console.log(err);
    } else {
      console.log(results)
    }
    callback(results);
  })
}

var _streamtool = new Streamtool()
module.exports = _streamtool;
