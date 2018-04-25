package edu.ksu.cis.macr.ipds.self.capabilities.admin;

import edu.ksu.cis.macr.organization.model.identifiers.ClassIdentifier;
import edu.ksu.cis.macr.organization.model.identifiers.UniqueIdentifier;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 Link the string names of capabilities to actual class names. Allows for spaces, etc in the models.
 */
public enum SelfControlCapabilities {
  INSTANCE;
  private static final HashMap<String, UniqueIdentifier> map = new HashMap<String, UniqueIdentifier>(){{
    put("SelfControlCapability", new ClassIdentifier(SelfControlCapability.class));
  }
    private static final long serialVersionUID = 1566684500067571212L;

  };

  public static Map<String, UniqueIdentifier> getMap() {
    return Collections.unmodifiableMap(map);
  }


}
