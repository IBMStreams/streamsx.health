package com.ibm.streamsx.health.vines.test;

import org.junit.Assert;
import org.junit.Test;

import com.ibm.streamsx.health.vines.VinesParser;
import com.ibm.streamsx.health.vines.model.Channel;
import com.ibm.streamsx.health.vines.model.ITerm;
import com.ibm.streamsx.health.vines.model.ITermValue;
import com.ibm.streamsx.health.vines.model.Location;
import com.ibm.streamsx.health.vines.model.Name;
import com.ibm.streamsx.health.vines.model.NameList;
import com.ibm.streamsx.health.vines.model.Patient;
import com.ibm.streamsx.health.vines.model.Service;
import com.ibm.streamsx.health.vines.model.ServiceId;
import com.ibm.streamsx.health.vines.model.Term;
import com.ibm.streamsx.health.vines.model.TermArray;
import com.ibm.streamsx.health.vines.model.TermValueMap;
import com.ibm.streamsx.health.vines.model.TermValueString;
import com.ibm.streamsx.health.vines.model.Terms;
import com.ibm.streamsx.health.vines.model.Vines;

public class Tests {

	private static final String simpleMessage = "{ \"_id\" : { \"$oid\" : \"581384c0d4355e094c65e542\" }, \"Data\" : { \"Header\": { \"ObjectSubType\": \"INFUS\", \"GroupId\": \"1\", \"IsCompleted\": \"False\", \"TransactionId\": \"cd1f8489-278d-4f1a-a672-71b91c2cae29\", \"DeviceId\": \"2\" }, \"Body\": { \"UpdatedBy\": \"Vines\", \"UpdatedTime\": \"2014-05-22T16:02:30.0801253Z\", \"StartTime\": \"2010-07-06T19:36:29-05:00\", \"EndTime\": \"2010-07-06T19:36:29-05:00\", \"PlacerId\": \"AB12345^PCD-03\", \"FillerId\": \"CD12345^HL7^ACDE48234567ABCD^EUI-64\", \"ServiceId\": { \"Dopamine 475\": { \"Code\": \"2222\" } }, \"VMD\": { \"MDC_DEV_PUMP_INFUS_VMD\": { \"Code\": \"69986\" } }, \"Terms\": { \"CH1\": { \"MDCX_ATTR_EVT_COND\": { \"Code\": \"0\", \"Value\": { \"MDCX_PUMP_DELIV_STOP\": { \"Code\": \"0\" } }, \"Date\": \"2010-07-06T19:36:29-05:00\" }, \"MDC_DEV_PUMP_INFUS_CHAN_DELIVERY\": { \"Code\": \"126978\" }, \"MDC_PUMP_STAT\": { \"Code\": \"184508\", \"Value\": \"^pump-status-paused\", \"Date\": \"2010-07-06T19:36:29-05:00\" }, \"MDC_PUMP_MODE\": {\"Code\": \"184504\", \"Value\": \"^pump-mode-485 continuous\", \"Date\": \"2010-07-06T19:36:29-05:00\" }, \"MDC_FLOW_FLUID_PUMP\": { \"Code\": \"157784\", \"Value\": \"0\", \"UOM\": \"MDC_DIM_MILLI_L_PER_HR\", \"UOMCode\": \"265266\", \"Date\": \"2010-07-06T19:36:29-05:00\" } }, \"CH2\": { \"MDC_DEV_PUMP_INFUS_CHAN_SOURCE\": { \"Code\": \"126977\" }, \"MDC_DRUG_NAME_TYPE\": { \"Code\": \"184330\", \"Value\": \"Dopamine\", \"Date\": \"20100706163629-490 0800\" }, \"MDC_CONC_DRUG\": { \"Code\": \"157760\", \"Value\": \"1.6\", \"UOM\": \"MDC_DIM_MILLI_G_PER_ML\", \"UOMCode\": \"264306\", \"Date\": \"2010-07-06T19:36:29-05:00\" }, \"MDC_RATE_DOSE\": { \"Code\": \"157924\", \"Value\": \"7\", \"UOM\": \"MDC_DIM_MICRO_G_PER_KG_PER_MIN\", \"UOMCode\": \"265619\", \"Date\": \"2010-07-06T19:36:29-05:00\" }, \"MDC_FLOW_FLUID_PUMP\": { \"Code\": \"157784\", \"Value\": \"24.9\", \"UOM\": \"MDC_DIM_MILLI_L_PER_HR\", \"UOMCode\": \"265266\", \"Date\": \"2010-07-06T19:36:29-05:00\" }, \"MDC_VOL_FLUID_TBI\": { \"Code\": \"999999\", \"Value\": \"250\", \"UOM\": \"MDC_DIM_MILLI_L\", \"UOMCode\": \"263762\",\"Date\": \"2010-07-06T19:36:29-05:00\" }, \"MDC_VOL_FLUID_TBI_REMAIN\": { \"Code\": \"157872\", \"Value\": \"224.4\", \"UOM\": \"MDC_DIM_MILLI_L\", \"UOMCode\": \"263762^MDC_DIM_MILLI_L^M500 DC^mL^mL^UCUM\", \"Date\": \"2010-07-06T19:36:29-05:00\" }, \"MDC_VOL_FLUID_DELIV\": { \"Code\": \"157864\", \"Value\": \"25.6\", \"UOM\": \"MDC_DIM_MILLI_L\", \"UOMCode\": \"263762\", \"Date\": \"2010-07-06T19:36:29-05:00\" }, \"MDC_VOL_FLUID_DELIV_TOTAL_SET\": { \"Code\": \"999999\", \"Value\": \"25.6\", \"UOM\": \"MDC_DIM_MILLI_L\", \"UOMCode\": \"263762\", \"Date\": \"2010-07-06T19:36:29-05:00\" } } }, \"Id\": \"5501e53e-7480-49ff-9e65-509da2672951\", \"TherapyId\": \"0eff7854-6cc9-4a3a-b0e3-19147e058bac\" }, \"Patient\": { \"_id\": \"537e1f98db23c810388ae214\", \"MRN\": \"HO60002\", \"MRNSource\": \"VinesIHEAddin\", \"Sex\": \"M\", \"NameList\": [ { \"FirstName\": \"Charles\", \"LastName\": \"Darwin\", \"MiddleName\": \"Robert\", \"Prefix\": \"Mr.\", \"Suffix\": \"I\" } ] }, \"Location\": { \"_id\": \"537e1f98db23c810388ae216\", \"Name\": \"Default Patient Location\", \"IsDeleted\": false,\"PatientId\": \"537e1f98db23c810388ae214\", \"IsDefault\": true } } }";
	private static final String waveformMessage = "{ \"Data\" : { \"Exchange\" : \"Wave\", \"Header\": { \"GroupId\": \"13\", \"IsCompleted\": \"False\", \"TransactionId\": \"4444444|SI_4\", \"DeviceId\": \"40\", \"ObjectSubType\": \"CVS\" }, \"Body\": { \"Id\": \"860f33e2-7fb0-4dfd-b878-ef17ec4e2ee3\", \"DeviceId\": \"40\", \"StartTime\": \"2014-10-27T16:47:20.128\", \"EndTime\": \"2014-10-27T16:47:20.130\", \"UpdatedBy\": \"DCPS-SERVICE-01-ff788387-8c37-42ae-930d-6f625a83035f\", \"UpdatedTime\": \"2014-10-27T16:49:14.3887514-05:00\", \"ServiceId\": { \"Wave\": { \"Code\": \"\" } }, \"Terms\": { \"CH01\": { \"MDC_ATTR_SAMP_RATE\": { \"Code\": \"\", \"Value\": \"128\", \"UOM\": \"MDC_DIM_SEC\", \"UOMCode\": \"262829\" }, \"MDC_ATTR_NU_MSMT_RES\": { \"Code\": \"\", \"Value\": \"0.0625\", \"UOM\": \"MDC_DIM_MMHG\", \"UOMCode\": \"266016\" }, \"MDC_ATTR_SCALE_RANGE\": { \"Code\": \"\", \"LowerValue\": \"-40\", \"UpperValue\": \"520\", \"UOM\": \"MDC_DIM_MMHG\", \"UOMCode\": \"266016\" }, \"MDC_ATTR_WAV_ENCODING\": { \"Code\": \"\", \"Value\": \"0\" }, \"MDC_ATTR_GRID_VIS\": { \"Code\": \"\", \"Value\": \"0^30^60^90^120^150^180\" }, \"MDC_ATTR_VIS_COLOR\": { \"Code\": \"\", \"Value\": \"255-0-0\" }, \"MDC_ATTR_EVENT\": [], \"MDC_ATTR_WAV\": [ { \"MDC_PRESS_BLD_ART_ABP\": { \"Value\": \"115.3125^116.5^117.4375^118.1875^118.8125^119.25^119.625^119.9375^120.125^120.25^120.125^119.875^119.375^118.75^117.9375^117^115.875^114.5^113^11 1.3125^109.8125^108.4375^107.3125^106.375^105.5625^104.875^104.25^103.6875^103.1875^102.8125^102.875^103.5625\" } } ] } } } } }";
	
	@Test
	public void _idTest() {
		Vines vines = VinesParser.fromJson(simpleMessage);		
		Assert.assertEquals("581384c0d4355e094c65e542", vines.get_id().getOid());
	}
	
	@Test
	public void bodyTest() {
		Vines vines = VinesParser.fromJson(simpleMessage);
		
		Assert.assertEquals("Vines", vines.getData().getBody().getUpdatedBy());
		Assert.assertEquals("2014-05-22T16:02:30.0801253Z", vines.getData().getBody().getUpdatedTime());
		Assert.assertEquals("2010-07-06T19:36:29-05:00", vines.getData().getBody().getStartTime());
		Assert.assertEquals("2010-07-06T19:36:29-05:00", vines.getData().getBody().getEndTime());
		Assert.assertEquals("AB12345^PCD-03", vines.getData().getBody().getPlacerId());
		Assert.assertEquals("CD12345^HL7^ACDE48234567ABCD^EUI-64", vines.getData().getBody().getFillerId());
		Assert.assertEquals("5501e53e-7480-49ff-9e65-509da2672951", vines.getData().getBody().getId());
		Assert.assertEquals("0eff7854-6cc9-4a3a-b0e3-19147e058bac", vines.getData().getBody().getTherapyId());
	}
	
	@Test
	public void serviceIdTest() {
		Vines vines = VinesParser.fromJson(simpleMessage);
		
		ServiceId serviceId = vines.getData().getBody().getServiceId();
		Assert.assertTrue(serviceId.containsKey("Dopamine 475"));
		
		Service service = serviceId.get("Dopamine 475");
		Assert.assertEquals("2222", service.getCode());
	}
	
	@Test
	public void termsTest() {
		Vines vines = VinesParser.fromJson(simpleMessage);
		
		Terms terms = vines.getData().getBody().getTerms();
		Assert.assertTrue(terms.containsKey("CH1"));
		Assert.assertTrue(terms.containsKey("CH2"));
		
		Channel chan = terms.getChannel("CH1");
		Assert.assertTrue(chan.containsKey("MDCX_ATTR_EVT_COND"));
		Assert.assertTrue(chan.containsKey("MDC_DEV_PUMP_INFUS_CHAN_DELIVERY"));
		Assert.assertTrue(chan.containsKey("MDC_PUMP_STAT"));
		Assert.assertTrue(chan.containsKey("MDC_PUMP_MODE"));
		Assert.assertTrue(chan.containsKey("MDC_FLOW_FLUID_PUMP"));
		
		chan = terms.getChannel("CH2");
		Assert.assertTrue(chan.containsKey("MDC_DEV_PUMP_INFUS_CHAN_SOURCE"));
		Assert.assertTrue(chan.containsKey("MDC_DRUG_NAME_TYPE"));
		Assert.assertTrue(chan.containsKey("MDC_CONC_DRUG"));
		Assert.assertTrue(chan.containsKey("MDC_RATE_DOSE"));
		Assert.assertTrue(chan.containsKey("MDC_FLOW_FLUID_PUMP"));
		Assert.assertTrue(chan.containsKey("MDC_VOL_FLUID_TBI"));
		Assert.assertTrue(chan.containsKey("MDC_VOL_FLUID_TBI_REMAIN"));
		Assert.assertTrue(chan.containsKey("MDC_VOL_FLUID_DELIV"));
		Assert.assertTrue(chan.containsKey("MDC_VOL_FLUID_DELIV_TOTAL_SET"));
		
		/* Test Term containing a Map for the value */
		ITerm t = terms.getChannel("CH1").getTerm("MDCX_ATTR_EVT_COND");
		Assert.assertTrue(t instanceof Term);
		
		Term term = (Term)t;
		Assert.assertEquals("0", term.getCode());
		
		ITermValue itv = term.getValue();
		Assert.assertTrue(itv instanceof TermValueMap);
		
		TermValueMap termValue = (TermValueMap)itv;
		Assert.assertTrue(termValue.containsKey("MDCX_PUMP_DELIV_STOP"));
		
		t = termValue.get("MDCX_PUMP_DELIV_STOP");
		Assert.assertTrue(t instanceof Term);		
		
		term = (Term)t;
		Assert.assertEquals("0", term.getCode());
		
		/* Test Term containing a String for the value */
		t = terms.getChannel("CH2").getTerm("MDC_VOL_FLUID_TBI");
		Assert.assertTrue(t instanceof Term);
		
		term = (Term)t;
		Assert.assertEquals("999999", term.getCode());
		Assert.assertEquals("MDC_DIM_MILLI_L", term.getUOM());
		Assert.assertEquals("263762", term.getUOMCode());
		Assert.assertEquals("2010-07-06T19:36:29-05:00", term.getDate());
		
		itv = term.getValue();
		Assert.assertTrue(itv instanceof TermValueString);

		TermValueString tvs = (TermValueString)itv;
		Assert.assertEquals("250", tvs.getValue());
	}
	
	@Test
	public void testPatient() {
		Vines vines = VinesParser.fromJson(simpleMessage);
		Patient patient = vines.getData().getPatient();
		
		Assert.assertEquals("537e1f98db23c810388ae214", patient.get_id());
		Assert.assertEquals("HO60002", patient.getMRN());
		Assert.assertEquals("VinesIHEAddin", patient.getMRNSource());
		Assert.assertEquals("M", patient.getSex());
		
		NameList nameList = patient.getNameList();
		Assert.assertTrue(nameList.size() > 0);
		
		Name name = nameList.get(0);
		Assert.assertEquals("Charles", name.getFirstName());
		Assert.assertEquals("Darwin", name.getLastName());
		Assert.assertEquals("Robert", name.getMiddleName());
		Assert.assertEquals("Mr.", name.getPrefix());
		Assert.assertEquals("I", name.getSuffix());
	}
	
	@Test
	public void testLocation() {
		Vines vines = VinesParser.fromJson(simpleMessage);
		
		Location location = vines.getData().getLocation();
		Assert.assertEquals("537e1f98db23c810388ae216", location.get_id());
		Assert.assertEquals("Default Patient Location", location.getName());
		Assert.assertEquals(false, location.getIsDeleted());
		Assert.assertEquals("537e1f98db23c810388ae214", location.getPatientId());
		Assert.assertEquals(true, location.getIsDefault());
	}
	
	@Test
	public void testWaveformTerm() {
		Vines vines = VinesParser.fromJson(waveformMessage);
		
		Terms terms = vines.getData().getBody().getTerms();
		Assert.assertTrue(terms.containsKey("CH01"));
		
		/* Test waveform */
		Channel chan = terms.getChannel("CH01");
		Assert.assertTrue(chan.containsKey("MDC_ATTR_WAV"));
		
		ITerm t = chan.getTerm("MDC_ATTR_WAV");
		Assert.assertTrue(t instanceof TermArray);
		
		TermArray ta = (TermArray)t;
		Assert.assertTrue(ta.size() > 0);
		
		ITermValue itv = ta.get(0);
		Assert.assertTrue(itv instanceof TermValueMap);
		
		TermValueMap tvm = (TermValueMap)itv;
		Assert.assertTrue(tvm.containsKey("MDC_PRESS_BLD_ART_ABP"));
		
		t = tvm.get("MDC_PRESS_BLD_ART_ABP");
		Assert.assertTrue(t instanceof Term);
		
		Term term = (Term)t;
		itv = term.getValue();
		Assert.assertTrue(itv instanceof TermValueString);
		
		TermValueString tvs = (TermValueString)itv;
		String waveformString = "115.3125^116.5^117.4375^118.1875^118.8125^119.25^119.625^119.9375^120.125^120.25^120.125^119.875^119.375^118.75^117.9375^117^115.875^114.5^113^11 1.3125^109.8125^108.4375^107.3125^106.375^105.5625^104.875^104.25^103.6875^103.1875^102.8125^102.875^103.5625";
		Assert.assertEquals(waveformString, tvs.getValue());	
		
		/* Test vitals */
		t = chan.getTerm("MDC_ATTR_SCALE_RANGE");
		Assert.assertTrue(t instanceof Term);
		
		term = (Term)t;
		Assert.assertEquals("", term.getCode());
		Assert.assertEquals("-40", term.getLowerValue());
		Assert.assertEquals("520", term.getUpperValue());
		Assert.assertEquals("MDC_DIM_MMHG", term.getUOM());
		Assert.assertEquals("266016", term.getUOMCode());
	}
	
	@Test
	public void testExchange() {
		Vines vines = VinesParser.fromJson(waveformMessage);
		Assert.assertEquals("Wave", vines.getData().getExchange());
	}
	
	
}










