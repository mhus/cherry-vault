/**
 * Copyright 2018 Mike Hummel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.mhus.cherry.vault.core.rest;

import java.util.Date;
import java.util.List;

import org.codehaus.jackson.node.ObjectNode;

import aQute.bnd.annotation.component.Component;
import de.mhus.cherry.vault.api.CherryVaultApi;
import de.mhus.cherry.vault.api.model.VaultEntry;
import de.mhus.cherry.vault.core.impl.StaticAccess;
import de.mhus.lib.core.MApi;
import de.mhus.lib.core.MDate;
import de.mhus.lib.core.MProperties;
import de.mhus.lib.core.pojo.PojoModelFactory;
import de.mhus.lib.core.util.EmptyList;
import de.mhus.lib.errors.MException;
import de.mhus.lib.errors.UsageException;
import de.mhus.osgi.sop.api.rest.AbstractObjectListNode;
import de.mhus.osgi.sop.api.rest.CallContext;
import de.mhus.osgi.sop.api.rest.JsonResult;
import de.mhus.osgi.sop.api.rest.RestNodeService;

@Component(immediate=true,provide=RestNodeService.class)
public class VaultNode extends AbstractObjectListNode<VaultEntry>{

	@Override
	public String[] getParentNodeIds() {
		return new String[] {ROOT_ID};
	}

	@Override
	public String getNodeId() {
		return "vault";
	}

	@Override
	protected List<VaultEntry> getObjectList(CallContext callContext) throws MException {
		// we will not support browsing
		return new EmptyList<>();
	}

	@Override
	public Class<VaultEntry> getManagedClass() {
		return VaultEntry.class;
	}

	@Override
	protected VaultEntry getObjectForId(CallContext context, String id) throws Exception {
		int p = id.indexOf(':');
		if (p <= 0) return null;
		String target = id.substring(p+1);
		id = id.substring(0, p);
		CherryVaultApi api = MApi.lookup(CherryVaultApi.class);
		return api.getSecret(id, target);
	}

	@Override
	public void doUpdate(JsonResult result, CallContext callContext) throws Exception {
		String secret = callContext.getParameter("_secret");
		String secretId = callContext.getParameter("_secretId");
		if (secretId == null) {
			secretId = getIdFromContext(callContext);
			int p = secretId.indexOf(':');
			if (p >= 0)
				secretId = secretId.substring(0, p); // ignore target
		}
		if (secretId == null)
			throw new UsageException("secret id not found");
		Date validFrom = MDate.toDate(callContext.getParameter("_validFrom"), null);
		Date validTo = MDate.toDate(callContext.getParameter("_validTo"), null);
		
		MProperties properties = new MProperties();
		for (String name : callContext.getParameterNames())
			if (!name.startsWith("_"))
				properties.put(name, callContext.getParameter(name));
			else
			if (name.startsWith("__"))
				properties.put(name.substring(1), callContext.getParameter(name));
		
		CherryVaultApi api = MApi.lookup(CherryVaultApi.class);
		
		if (secret != null) {
			api.importUpdate(secretId, validFrom, validTo, secret, properties);
		} else {
			api.createUpdate(secretId, validFrom, validTo, properties);
		}
		
	}
	@Override
	public void doCreate(JsonResult result, CallContext callContext) throws Exception {
		String groupName = callContext.getParameter("_group");
		String secret = callContext.getParameter("_secret");
		Date validFrom = MDate.toDate(callContext.getParameter("_validFrom"), null);
		Date validTo = MDate.toDate(callContext.getParameter("_validTo"), null);
		
		MProperties properties = new MProperties();
		for (String name : callContext.getParameterNames())
			if (!name.startsWith("_"))
				properties.put(name, callContext.getParameter(name));
			else
			if (name.startsWith("__"))
				properties.put(name.substring(1), callContext.getParameter(name));
		
		CherryVaultApi api = MApi.lookup(CherryVaultApi.class);
		
		if (secret != null) {
			String secretId = api.importSecret(groupName, validFrom, validTo, secret, properties);
			ObjectNode res = result.createObjectNode();
			res.put("secretId", secretId);
		} else {
			String secretId = api.createSecret(groupName, validFrom, validTo, properties);
			ObjectNode res = result.createObjectNode();
			res.put("secretId", secretId);
		}
		
	}
	@Override
	public void doDelete(JsonResult result, CallContext callContext) throws Exception {
		String secretId = callContext.getParameter("_secretId");
		if (secretId == null) {
			secretId = getIdFromContext(callContext);
			int p = secretId.indexOf(':');
			if (p >= 0)
				secretId = secretId.substring(0, p); // ignore target
		}
		if (secretId == null)
			throw new UsageException("secret id not found");

		CherryVaultApi api = MApi.lookup(CherryVaultApi.class);
		
		api.deleteSecret(secretId);
	}
	
	@Override
	protected PojoModelFactory getPojoModelFactory() {
		return StaticAccess.moManager.getManager().getPojoModelFactory();
	}
	
}