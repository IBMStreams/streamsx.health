package com.ibm.streamsx.health.analytics.wex.services;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.Arrays;

import com.ibm.streamsx.health.analytics.wex.internal.Microservice;
import com.ibm.streamsx.health.ingest.types.model.ClinicalNoteEvent;
import com.ibm.streamsx.topology.Topology;
import com.ibm.streamsx.topology.function.Supplier;

public abstract class AbstractWEXHealthService extends Microservice implements Serializable {

	private static final long serialVersionUID = 1L;

	private String wexToolkitPath;
	private Supplier<String> wexHostParam;
	private Supplier<Integer> wexPortParam;
	private Supplier<String> wexPatientFieldName;
	private Supplier<String> username;
	private Supplier<String> password;
	private Supplier<String> collectionName;
	
	public AbstractWEXHealthService(String serviceName, String wexToolkitPath) throws Exception {
		super(serviceName);
		this.wexToolkitPath = wexToolkitPath;
		
		Topology t = getTopology();
		addJars(getWexToolkitPath() + "/impl/lib");
		addJars(getWexToolkitPath() + "/opt/downloaded");
		t.addClassDependency(ClinicalNoteEvent.class);
		
		// create the submission-time parameters
		wexPatientFieldName = t.createSubmissionParameter("wex.patient.field.name", "");
		wexHostParam = t.createSubmissionParameter("wex.host", String.class);
		wexPortParam = t.createSubmissionParameter("wex.port", Integer.class);
		username = t.createSubmissionParameter("wex.username", "");
		password = t.createSubmissionParameter("wex.password", "");
		collectionName = t.createSubmissionParameter("collectionName", String.class);		
	}

	@Override
	public void build() throws Exception {

	}
	
	protected void addJars(String dirPath) throws Exception {
		File dir = new File(dirPath);
		if(!dir.exists()) {
			throw new FileNotFoundException(dir.getAbsolutePath());
		}
		File[] jarFiles = dir.listFiles(new FileFilter() {
			
			@Override
			public boolean accept(File pathname) {
				return pathname.isFile() && pathname.getName().endsWith(".jar");
			}
		});
		
		Arrays.asList(jarFiles).forEach(f -> {
			getTopology().addJarDependency(f.getAbsolutePath());
		});
	}
	
	
	public Supplier<String> getWexHostParam() {
		return wexHostParam;
	}
	
	public Supplier<Integer> getWexPortParam() {
		return wexPortParam;
	}
	
	public Supplier<String> getWexPatientFieldName() {
		return wexPatientFieldName;
	}
	
	public Supplier<String> getCollectionName() {
		return collectionName;
	}
	
	public String getWexToolkitPath() {
		return wexToolkitPath;
	}
	
	public Supplier<String> getUsername() {
		return username;
	}
	
	public Supplier<String> getPassword() {
		return password;
	}
}
