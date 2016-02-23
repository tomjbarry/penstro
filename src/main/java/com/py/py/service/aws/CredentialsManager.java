package com.py.py.service.aws;

import javax.annotation.PostConstruct;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;

public class CredentialsManager {

	protected AWSCredentials credentials = null;
	protected Region region;
	protected Regions regions;
	protected String profile;
	protected String credentialsLocation;
	
	@PostConstruct
	public void initialize() {
		if(profile == null || profile.isEmpty()) {
			profile = null;
		}
		if(credentialsLocation == null || credentialsLocation.isEmpty()) {
			credentials = new ProfileCredentialsProvider(profile).getCredentials();
		} else {
			credentials = new ProfileCredentialsProvider(credentialsLocation, profile).getCredentials();
		}
		region = Region.getRegion(regions);
	}
	
	public void setRegions(Regions regions) {
		this.regions = regions;
	}
	
	public void setProfile(String profile) {
		this.profile = profile;
	}
	
	public void setCredentialsLocation(String credentialsLocation) {
		this.credentialsLocation = credentialsLocation;
	}
	
	public AWSCredentials getCredentials() {
		return credentials;
	}
	
	public Region getRegion() {
		return region;
	}
	
}
