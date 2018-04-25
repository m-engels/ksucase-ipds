package edu.ksu.cis.macr.ipds.resources;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.net.URL;

/**
 Image files (e.g. the one used to show the layout in the connection window) could not be found when building with
 Gradle.  This is an attempt to build a generic solution.  http://stackoverflow.com/questions/10551772/java-resource-images-not-displaying
 */
public class Resources {
  private static final Logger LOG = LoggerFactory.getLogger
          (Resources.class);
  private static final boolean debug = false;

  public static ImageIcon getImage() {
      URL resourceUrl = Resources.class.getResource("01nodemap.png");
      if (resourceUrl != null) {
          LOG.info("Using resourceUrl = {}", resourceUrl.toString());
          if (debug) LOG.debug("resourceUrl={}", resourceUrl.toString());
          return new ImageIcon(resourceUrl);
      }
      System.exit(-5);
      return null;
  }
}