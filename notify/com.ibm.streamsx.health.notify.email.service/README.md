# Streams Healthcare Email Notification Service

This service sends an email notification upon receipt of a tuple.

# Dependencies

This service depends on the following toolkits: 

  * com.ibm.streamsx.topology
  * com.ibm.streamsx.mail

# Expected Input

This service expects an `JsonType` tuple as the input: 

```
{
  "jsonString" : string
}
```

# Output

None 

# Build

Run the following command to build this service: 

`gradle build`


# Configure

The service properties can be set in the `service.properties` file. The following properties are available.
Properties must be prefixed with `com.ibm.streamsx.health.notify.email.service.EmailService`

| Property | Description | Default |
| --- | --- | :---: |
| subTopic | Topic to subscribe data from.  JsonString will be sent out as the email content |  |
| username | The username to the email server | |
| password | The password to the email server |  |
| host | Host to email server |  |
| port | Port to email server | Default is 587  |
| authentication | Authentication method to email server | Default is TLS |
| subject | Subject line for the email | Default "Notification from IBM Streams" |
| from | Originator of email | Default "IBM Streams" |
| to | Recipient of email |  |


# Before you execute

This service depends on having the streamsx.mail toolkit to be part of the STREAMS_SPLPATH.

Before executing this service for the first time:

1.  Follow instructions from here to download and build toolkit:  [streamsx.mail](https://github.com/IBMStreams/streamsx.mail)
1.  `export STREAMS_SPLPATH=location_of_mail_toolkit:$STREAMS_SPLPATH`

# Execute

Run the following command to launch the service:

`gradle execute`



