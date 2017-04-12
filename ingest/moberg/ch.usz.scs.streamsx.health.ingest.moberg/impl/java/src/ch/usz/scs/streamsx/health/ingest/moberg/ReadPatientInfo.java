package ch.usz.scs.streamsx.health.ingest.moberg;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.log4j.Logger;

import com.ibm.streams.operator.AbstractOperator;
import com.ibm.streams.operator.OperatorContext;
import com.ibm.streams.operator.OutputTuple;
import com.ibm.streams.operator.StreamingData.Punctuation;
import com.ibm.streams.operator.StreamingInput;
import com.ibm.streams.operator.StreamingOutput;
import com.ibm.streams.operator.Tuple;
import com.ibm.streams.operator.model.InputPortSet;
import com.ibm.streams.operator.model.InputPortSet.WindowMode;
import com.ibm.streams.operator.model.InputPortSet.WindowPunctuationInputMode;
import com.ibm.streams.operator.model.InputPorts;
import com.ibm.streams.operator.model.OutputPortSet;
import com.ibm.streams.operator.model.Parameter;
import com.ibm.streams.operator.model.OutputPortSet.WindowPunctuationOutputMode;

import ch.usz.scs.streamsx.health.ingest.moberg.patientinfo.PatientInfo;
import ch.usz.scs.streamsx.health.ingest.moberg.patientinfo.PatientInfoReader;

import com.ibm.streams.operator.model.OutputPorts;
import com.ibm.streams.operator.model.PrimitiveOperator;

/**
 * reads the patient.info file
 */
@PrimitiveOperator(name="ReadPatientInfo", namespace="ch.usz.scs.streamsx.health.ingest.moberg", description="Java Operator ReadPatientInfo")
@InputPorts({@InputPortSet(description="Port that ingests tuples", cardinality=1, optional=false, windowingMode=WindowMode.NonWindowed, windowPunctuationInputMode=WindowPunctuationInputMode.Oblivious)})
@OutputPorts({@OutputPortSet(description="Port that produces tuples", cardinality=1, optional=false, windowPunctuationOutputMode=WindowPunctuationOutputMode.Preserving)})
public class ReadPatientInfo extends AbstractOperator {
	
	private Logger log = Logger.getLogger(this.getClass());
	
	private PatientInfoReader infoReader;
	private static DateTimeFormatter microSecondsFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");
	
    /**
     * the root folder of the CNS archive
     * @param value
     */
    @Parameter
    public void setCnsArchiveRoot(String value) {
    	archiveRoot = value;
    }
    
	private String archiveRoot;
	
    /**
     * Initialize this operator. Called once before any tuples are processed.
     * @param context OperatorContext for this operator.
     * @throws Exception Operator failure, will cause the enclosing PE to terminate.
     */
	@Override
	public synchronized void initialize(OperatorContext context)
			throws Exception {
		super.initialize(context);
        log.trace("init start");
 
		log.trace(LogHelper.getMessage(context, LogHelper.INIT_MESSAGE));
		
		try {
			infoReader = new PatientInfoReader();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.error(e.getMessage());
		}
		
        log.trace("init finished");
    }

    /**
     * Notification that initialization is complete and all input and output ports 
     * are connected and ready to receive and submit tuples.
     * @throws Exception Operator failure, will cause the enclosing PE to terminate.
     */
    @Override
    public synchronized void allPortsReady() throws Exception {
    	// This method is commonly used by source operators. 
    	// Operators that process incoming tuples generally do not need this notification. 
        OperatorContext context = getOperatorContext();
        
        log.trace(LogHelper.getMessage(context, LogHelper.PORTS_READY_MESSAGE));
    }

    /**
     * Process an incoming tuple that arrived on the specified port.
     * <P>
     * Copy the incoming tuple to a new output tuple and submit to the output port. 
     * </P>
     * @param inputStream Port the tuple is arriving on.
     * @param tuple Object representing the incoming tuple.
     * @throws Exception Operator failure, will cause the enclosing PE to terminate.
     */
    @Override
    public final void process(StreamingInput<Tuple> inputStream, Tuple tuple)
            throws Exception {

    	// Create a new tuple for output port 0
        StreamingOutput<OutputTuple> outStream = getOutput(0);
        OutputTuple outTuple = outStream.newTuple();
        outTuple.assign(tuple);

        // compose path to patient.info file
        String patientId = tuple.getString(Conventions.PATIENT_ID_ATTRIBUTE);
		log.info("Process patient with patiend id: " + patientId);

        if (!patientId.equals("")) {
        	// read patient info from file
        	String patientInfoFilename = CnsFileStructure.PATIENT_DIRECTORY_PREFIX + patientId;
	        Path patientInfoPath = Paths.get(archiveRoot, patientInfoFilename, CnsFileStructure.PATIENT_INFO_FILE);
	        PatientInfo patientInfo = infoReader.readXmlFile(patientInfoPath.toFile());
	        
			log.info("Process patient with medical record number: " + patientInfo.getMedicalRecordNumber());

	        
	        if (!patientInfo.getMedicalRecordNumber().isEmpty()) {
		        
		        // create timestamp
		        LocalDateTime timestamp = LocalDateTime.now();
		        
		        
		        // read patient info from file and set relevant properties to the output tuple
		        outTuple.setString(Conventions.PATIENT_INFO_FIRSTNAME, patientInfo.getPatientFirstName());
		        outTuple.setString(Conventions.PATIENT_INFO_LASTNAME, patientInfo.getPatientLastName());
		        outTuple.setString(Conventions.PATIENT_INFO_MEDICAL_RECORD_NUMBER, patientInfo.getMedicalRecordNumber());
		        outTuple.setString(Conventions.PATIENT_INFO_PHYSICIAN_NAME, patientInfo.getPhysicianName());
		        outTuple.setString(Conventions.PATIENT_INFO_GENDER, patientInfo.getGender());
		        outTuple.setString(Conventions.PATIENT_INFO_BIRTHDATE, patientInfo.getBirthdate());
		        outTuple.setString(Conventions.PATIENT_INFO_TIMESTAMP,  timestamp.format(microSecondsFormatter));
		        outTuple.setString(Conventions.PATIENT_INFO_ADDITIONAL_INFO, patientInfo.getAdditionalInfo());
		        outTuple.setLong(Conventions.PATIENT_INFO_SYSTEM_OFFSET, Long.parseLong(patientInfo.getSystemOffset()));
		        outTuple.setBoolean(Conventions.PATIENT_INFO_ISVALID, true);
	        }
	        else {
	        	// mrn not set
		        outTuple.setBoolean(Conventions.PATIENT_INFO_ISVALID, false);
	        }
        }
        else {
        	// no patientId read
	        outTuple.setBoolean(Conventions.PATIENT_INFO_ISVALID, false);
        }
	    
        outTuple.setString(Conventions.CNS_INFO, archiveRoot);


        outStream.submit(outTuple);
    }
    
    /**
     * Process an incoming punctuation that arrived on the specified port.
     * @param stream Port the punctuation is arriving on.
     * @param mark The punctuation mark
     * @throws Exception Operator failure, will cause the enclosing PE to terminate.
     */
    @Override
    public void processPunctuation(StreamingInput<Tuple> stream,
    		Punctuation mark) throws Exception {
    	super.processPunctuation(stream, mark);
    }

    /**
     * Shutdown this operator.
     * @throws Exception Operator failure, will cause the enclosing PE to terminate.
     */
    public synchronized void shutdown() throws Exception {
        OperatorContext context = getOperatorContext();
        log.trace(LogHelper.getMessage(context, LogHelper.SHUTDOWN_MESSAGE));
        super.shutdown();
    }
}
