package ch.usz.scs.streamsx.health.ingest.moberg.patientinfo;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by ssuter on 18.09.2015.
 * Copied over from mock - with some modifications
 */
@XmlRootElement(name = "PatientInfo")
@XmlAccessorType(XmlAccessType.FIELD)
public class PatientInfo {

    @XmlElement
    private String FileVersion = "";

    @XmlElement
    private String SoftwareVersion = "";

    @XmlElement
    private String SystemName = "";

    @XmlElement
    private String MACAddr = "";

    @XmlElement
    private String PatientFirstName = "";

    @XmlElement
    private String PatientLastName = "";

    @XmlElement
    private String MedicalRecordNumber = "";

    @XmlElement
    private String PhysicianName = "";

    @XmlElement
    private String Gender = "";

    @XmlElement
    private String Birthdate = "";

    @XmlElement
    private String AdditionalInfo = "";

    @XmlElement
    private String TimeStamp = "";

    @XmlElement
    private String SystemOffset = "";

    @XmlElement
    private String RecordingEndTime = "";

    @XmlElement
    private String Protocol = "";

    public PatientInfo() {
    }

    public String getFileVersion() {
        return FileVersion.trim();
    }

    public String getSoftwareVersion() {
        return SoftwareVersion.trim();
    }

    public String getSystemName() {
        return SystemName.trim();
    }

    public String getMACAddr() {
        return MACAddr.trim();
    }

    public String getProtocol() {
        return Protocol.trim();
    }

    public String getPatientFirstName() {
        return PatientFirstName.trim();
    }

    public String getPatientLastName() {
        return PatientLastName.trim();
    }

    public String getMedicalRecordNumber() {
        return MedicalRecordNumber.trim();
    }

    public String getPhysicianName() {
        return PhysicianName.trim();
    }

    public String getGender() {
        return Gender.trim();
    }

    public String getBirthdate() {
        return Birthdate.trim();
    }

    public String getAdditionalInfo() {
        return AdditionalInfo.trim();
    }

    public String getTimeStamp() {
        return TimeStamp.trim();
    }

    public String getSystemOffset() {
        return SystemOffset.trim();
    }

    public String getRecordingEndTime() {
        return RecordingEndTime.trim();
    }
}
