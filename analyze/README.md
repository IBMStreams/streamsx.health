# streamsx.health.analyze

This component contains services that provide base analytics for vital or waveform data.

* com.ibm.streamsx.health.analytics.ecg - This service provides base analytics for ECG waveforms.  Initial contribution includes a QRS beat detector based on this project:  [OSEA-4-Java](https://github.com/MEDEVIT/OSEA-4-Java)
* vital_ews - This service calculates patient's Early Warning Score (EWS) based on vitals data.  
