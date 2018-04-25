package edu.ksu.cis.macr.ipds.message;

import org.junit.runner.JUnitCore;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 The class {@code TestAll} builds a suite that can be used to run all of the tests within its package as well as within
 any subpackages of its package.

 @author Denise Case
 @version $Revision: 1.0 $
 @generatedBy CodePro at 6/25/14 3:02 PM */
@RunWith(Suite.class)
@Suite.SuiteClasses({
//        IMessageTest.class,
//        MessageTest.class,
})
public class TestAll {

  /**
   Launch the test.

   @param args the command line arguments
   @generatedBy CodePro at 6/25/14 3:02 PM
   */
  public static void main(String[] args) {
    JUnitCore.runClasses(TestAll.class);
  }
}
