package ch.usz.scs.streamsx.health.ingest.moberg;

import java.io.IOException;
import java.nio.file.*;

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
import com.ibm.streams.operator.model.OutputPorts;
import com.ibm.streams.operator.model.PrimitiveOperator;

/**
 * Read the id of the currently admitted patient from the CNS share 
 */
@PrimitiveOperator(name="ReadPatientId", namespace="ch.usz.scs.streamsx.health.ingest.moberg", description="Java Operator ReadPatientId")
@InputPorts({@InputPortSet(description="control signal", cardinality=1, optional=false, windowingMode=WindowMode.NonWindowed, windowPunctuationInputMode=WindowPunctuationInputMode.Oblivious)})
@OutputPorts({@OutputPortSet(description="control signal with patientId", cardinality=1, optional=false, windowPunctuationOutputMode=WindowPunctuationOutputMode.Preserving)})
public class ReadPatientId extends AbstractOperator {
	
	private Logger log = Logger.getLogger(this.getClass());

    /**
     * the root folder of the CNS archive
     * @param value
     */
    @Parameter
    public void setCnsArchiveRoot(String value) {
    	filename = Paths.get(value, CnsFileStructure.ADMITTED_PATIENT_FILE);
    }
    
	private Path filename;
    
    /**
     * Initialize this operator. Called once before any tuples are processed.
     * @param context OperatorContext for this operator.
     * @throws Exception Operator failure, will cause the enclosing PE to terminate.
     */
	@Override
	public synchronized void initialize(OperatorContext context)
			throws Exception {
 		super.initialize(context);
        log.trace(LogHelper.getMessage(context, LogHelper.INIT_MESSAGE));
	}
    
    /**
     * Notification that initialization is complete and all input and output ports 
     * are connected and ready to receive and submit tuples.
     * @throws Exception Operator failure, will cause the enclosing PE to terminate.
     */
    @Override
    public synchronized void allPortsReady()
    		throws Exception {
        OperatorContext context = getOperatorContext();
        log.trace(LogHelper.getMessage(context, LogHelper.PORTS_READY_MESSAGE));
    }

    /**
     * Process incoming 
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

        // Copy across all matching attributes.
        outTuple.assign(tuple);
        
        // read patient id and append it to the tuple
        String patientId;
		try {
			patientId = Files.readAllLines(filename).get(0).trim();
			outTuple.setString(Conventions.PATIENT_ID_ATTRIBUTE, patientId); 
		} catch (IOException e) {
			// file not found (during patient change, or CNS switched off)
			// empty string needs to be interpreted this way by recipients
			outTuple.setString(Conventions.PATIENT_ID_ATTRIBUTE, ""); 
		}
 
        // Submit new tuple to output port 0
        outStream.submit(outTuple);
    }
    
    /**
     * Process an incoming punctuation that arrived on the specified port.
     * @param stream Port the punctuation is arriving on.
     * @param mark The punctuation mark
     * @throws Exception Operator failure, will cause the enclosing PE to terminate.
     */
    @Override
    public void processPunctuation(StreamingInput<Tuple> stream, Punctuation mark)
    		throws Exception {
    	super.processPunctuation(stream, mark);
    }

    /**
     * Shutdown this operator.
     * @throws Exception Operator failure, will cause the enclosing PE to terminate.
     */
    public synchronized void shutdown()
    		throws Exception {
        OperatorContext context = getOperatorContext();
        log.trace(LogHelper.getMessage(context, LogHelper.SHUTDOWN_MESSAGE));
        super.shutdown();
    }
}
