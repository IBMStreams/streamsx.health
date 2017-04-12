package ch.usz.scs.streamsx.health.ingest.moberg.patientinfo;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;

/**
 * Created by ssuter on 18.09.2015.
 * copied over from mock
 */
public final class PatientInfoReader {
	
	private Unmarshaller unmarshaller;
	
	public PatientInfoReader() throws JAXBException {
        JAXBContext jc = JAXBContext.newInstance(PatientInfo.class);
        unmarshaller = jc.createUnmarshaller();
	}

    public PatientInfo readXmlFile(File file) throws Exception {
        PatientInfo result = (PatientInfo) unmarshaller.unmarshal(file);
        return result;
    }

}