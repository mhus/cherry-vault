package de.mhus.cherry.vault.api.model;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Date;

import org.mongodb.morphia.annotations.PrePersist;

import de.mhus.lib.core.IReadProperties;
import de.mhus.lib.core.MProperties;
import de.mhus.lib.core.util.ReadOnlyException;
import de.mhus.lib.mongo.MoMetadata;

public class VaultEntry extends MoMetadata {

	protected String target;
	protected String group;
	protected String secretKeyId;
	protected String secret;
	protected String secretId;
	protected MProperties meta;
	protected Date validFrom;
	protected Date validTo;
	
	private String checksum;
	
	// constructor for morphia
	public VaultEntry() {}
	
	// constructor for VaultArchive
	public VaultEntry(VaultEntry clone) {
		target = clone.getTarget();
		group = clone.getGroup();
		secretKeyId = clone.getSecretKeyId();
		secretId = clone.getSecretId();
		secret = clone.getSecret();
		meta = new MProperties(clone.getMeta());
		validFrom = clone.getValidFrom();
		validTo = clone.getValidTo();
	}
	
	@PrePersist
	public void preChecksum() throws NoSuchAlgorithmException, UnsupportedEncodingException, ReadOnlyException {
		MessageDigest md = MessageDigest.getInstance("SHA-256");

		md.update(secret.getBytes("UTF-8"));
		md.update(secretKeyId.getBytes("UTF-8"));
		md.update(secretId.getBytes("UTF-8"));
		md.update(target.getBytes("UTF-8"));
		md.update(group.getBytes("UTF-8"));

		byte[] digest = md.digest();
		String cs = Base64.getEncoder().encodeToString(digest);
		
		if (checksum == null)
			checksum = cs;
		else
		if (!cs.equals(checksum))
			throw new ReadOnlyException("VautlEntry data are read only",getObjectId());
		
	}
	
	public String getTarget() {
		return target;
	}

	public String getGroup() {
		return group;
	}

	public String getSecret() {
		return secret;
	}

	public IReadProperties getMeta() {
		if (meta == null) meta = new MProperties();
		return meta;
	}


	public String getSecretKeyId() {
		return secretKeyId;
	}


	public String getSecretId() {
		return secretId;
	}

	public Date getValidFrom() {
		return validFrom;
	}

	public void setValidFrom(Date validFrom) {
		this.validFrom = validFrom;
	}

	public Date getValidTo() {
		return validTo;
	}

	public void setValidTo(Date validTo) {
		this.validTo = validTo;
	}
	
}
