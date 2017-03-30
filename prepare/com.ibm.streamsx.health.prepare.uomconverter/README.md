# Unit of Measure Conversion Service

The **com.ibm.streamsx.health.prepare.uomconverter** is capable of converting reading values in *Observation* tuples from one unit of measure to another. The unit of measures that are currently supported for conversion are: 

  * **Voltage** - convert voltage from one metric value to another (i.e. V to mV)
  * **Temperature** - convert Celcius to/from Farhenheit
  * **Frequency** - convert beats per minute to/from beats per second

After conversion, the UOM Conversion service returns an *Observation* tuple containing the converted value and new unit of measure. If a mapping does not exist for the unit of measure set in the *Observation* tuple, then the input tuple is passed through unchanged. 


## Mapping File

The UOM Service relies on a user-defined mapping file to indicate the units that should be converted. The mapping file should contain a separate mapping per line. The format of a mapping is as follows: 

  `<fromUnit>|<toUnit>`

where `<fromUnit>` is the unit you want to convert from, and `<toUnit>` is the unit you want to convert to. Here is an example of a mapping file where all milli volt values are converted to micro volt and all Fahrenheit temperatures are converted to Celcius: 

```
mV|uV
F|C
```

### Supported Unit Formats

The following is a list of unit formats that are supported. 

**IMPORTANT**: Unit spellings are ***case-sensitive***. Misspelling a unit may result in the service failing to run. 

#### Voltage

| Unit | Description | | Unit | Description |
| :---: | --- | --- | :---: | --- |
| YV | yotta-volt | | cV | centi-volt |
| ZV | zetta-volt | | mV | milli-volt |
| EV | exa-volt | | uV | micro-volt |
| PV | peta-volt | | µV | micro-volt |
| TV | terra-volt | | nV | nano-volt |
| GV | giga-volt | | pV | pico-volt |
| MV | mega-volt | | fV | femto-volt |
| kV | kilo-volt | | aV | atto-volt 
| hV | hecto-volt | | zV | zepto-volt |
| daV | deka-volt | | yV | yocto-volt |
| dV | deci-volt |


#### Temperature

| Unit | Description |
| :---: | --- |
| C | Celcius |
| °C | Celcius |
| F | Fahrenheit |
| °F | Fahrenheit |

#### Frequency 

| Unit | Description |
| :---: | --- |
| bpm | beats per minute |
| bps | beats per second |


# Dependencies

This service contains the following dependencies: 

  * **com.ibm.streamsx.health.ingest** toolkit
  * Apache Common CLI v1.3.1
  * Units of Measurement SE v1.0.4 (https://github.com/unitsofmeasurement/uom-se)
  * Units of Measurement Systems v0.5 (https://github.com/unitsofmeasurement/uom-systems)

# Expected Input

This service expects an `Observation` tuple as input: 

```
{
  "patientId" : string,
  "device" : {
    "id" : string,
    "locationId" : string
  },
  "readingSource" : {
    "id" : string,
    "sourceType" : string,
    "deviceId" : string
  },
  "reading" : {
    "ts" : numeric,
    "readingType" : {
      "system" : string,
      "code" : string
    },
    "value" : numeric,
    "uom" : string
  }
}
```

# Output

  * **Published Topics:** "prepare-uom-converter"
  * **Output JSON Schema:** [Observation Type](https://github.com/IBMStreams/streamsx.health/wiki/Observation-Data-Type)

```
{
  "patientId" : string,
  "device" : {
    "id" : string,
    "locationId" : string
  },
  "readingSource" : {
    "id" : string,
    "sourceType" : string,
    "deviceId" : string
  },
  "reading" : {
    "ts" : numeric,
    "readingType" : {
      "system" : string,
      "code" : string
    },
    "value" : numeric,
    "uom" : string
  }
}
```


# Build

Run the following command to build the service:

`gradle build`


# Execute

The service properties, including the path to the mapping file, can be set in the `uom.service.properties` file. The following properties are available: 

| Property | Description | Default |
| --- | --- | :---: |
| **mapFile** | A relative or absolute path to the mapping file. | `./unitmapping.properties` |
| **subscriptionTopic** | The topic that the service should subscribe to. | `ingest-beacon` |
| **debug** | Enables and disables debug mode (`true` or `false`) | `false` |

To submit the service the local instance, run the following command: 

`gradle execute`
