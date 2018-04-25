package edu.ksu.cis.macr.ipds.primary.capabilities.manage;

import edu.ksu.cis.macr.aasis.agent.persona.AbstractOrganizationCapability;
import edu.ksu.cis.macr.aasis.agent.persona.IOrganization;
import edu.ksu.cis.macr.aasis.agent.persona.IPersona;
import edu.ksu.cis.macr.aasis.simulator.clock.Clock;
import edu.ksu.cis.macr.aasis.simulators.PhysicalSystemSimulator;
import edu.ksu.cis.macr.goal.model.InstanceParameters;
import edu.ksu.cis.macr.ipds.config.RunManager;
import edu.ksu.cis.macr.ipds.config.SmartInverterAlgorithm;
import edu.ksu.cis.macr.ipds.primary.actuators.ActuatorType;
import edu.ksu.cis.macr.ipds.primary.actuators.SmartInverterSetting;
import edu.ksu.cis.macr.ipds.primary.sensors.smartmeter.IElectricalData;
import edu.ksu.cis.macr.ipds.primary.sensors.smartmeter.ISmartMeterRead;
import edu.ksu.cis.macr.organization.model.InstanceGoal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.IOException;
import java.text.ParseException;
import java.util.Objects;

/**
 The {@code SmartInverterCapability} provides the ability to control a smart inverter.  Capabilities can get
 simulation settings from the Scenario object and dynamic parameters will be available from related goal guidelines when
 used in a plan.
 */
public class SmartInverterCapability extends AbstractOrganizationCapability implements edu.ksu.cis.macr.obaa_pp.actuator.IActuator, ISmartInverterCapability {

  private static final Logger LOG = LoggerFactory.getLogger(SmartInverterCapability.class);
  private static final boolean debug = false;
  private IPersona owner;
 // private TimeSeries data = new TimeSeries("SmartInverterSettings");
  private String smartInverterName;

  /**
   Construct a new {@code SmartInverterCapability} instance without the special attributes defined in agent.xml.

   @param owner - the agent possessing this capability.
   @param organization - the immediate organization in which this agent operates.
   */
  public SmartInverterCapability(final IPersona owner, final IOrganization organization) {
    super(SmartInverterCapability.class, owner, organization);
    this.smartInverterName = null;

  }


  /**
   Construct a new {@code SmartInverterCapability} instance with the special attributes defined in agent.xml.

   @param owner - the agent possessing this capability.
   @param organization - the immediate organization in which this agent operates.
   @param smartInverterName - the unique string name of this smart inverter.
   */
  public SmartInverterCapability(
          final IPersona owner,
          final IOrganization organization, final String smartInverterName) {
    super(SmartInverterCapability.class, owner, organization);
    setSmartInverterName(smartInverterName);
  }

  public static boolean reportingSmartInverterControlAction(final long timeSlice, final String smartInverterName) {
    RunManager.setTotalSmartInvertersReported(RunManager.getTotalSmartInvertersReported() + 1);

    if (RunManager.getTotalSmartInvertersReported() == 4) {
      RunManager.setTotalSmartInvertersReported(0);
      try {
        int newTimeSlice = Clock.getTimeSlicesElapsedSinceStart() + 1;
        Clock.setTimeSlicesElapsedSinceStart(newTimeSlice);
        PhysicalSystemSimulator.getSensorDataAndUpdateDisplay(newTimeSlice);
      } catch (ParseException e) {
        e.printStackTrace();
      }
      return true;
    }
    return false;
  }

  /**
   Sets a control action or otherwise acts in the environment at the default time slice.

   @param setting - the desired control action inverter setting
   @return - an int indicating whether the action was taken
   @throws java.io.IOException - Handles all IO Exceptions
   @throws ClassNotFoundException - Handles any ClassNotFound Exception
   @throws edu.ksu.cis.macr.obaa_pp.actuator.ActuatorOutOfServiceException - Handles any ActuatorOutOfService Exception
   @throws edu.ksu.cis.macr.obaa_pp.actuator.SettingOutofRangeException - Handles any SettingOutofService Exception
   */
  @Override
  public int act(edu.ksu.cis.macr.obaa_pp.actuator.ISetting<?> setting)
          throws IOException, ClassNotFoundException, edu.ksu.cis.macr.obaa_pp.actuator.ActuatorOutOfServiceException, edu.ksu.cis.macr.obaa_pp.actuator.SettingOutofRangeException {
    actAt(setting, Clock.getTimeSlicesElapsedSinceStart());
    return 1;
  }

  /**
   Sets a control action or otherwise acts in the environment at the specified time slice.

   @param setting - the setting object for this type of action
   @param timeSlice - the time slice at which the action should be taken
   @return -  an int indicating whether the action was taken
   @throws java.io.IOException - Handles any IO Exceptions
   @throws ClassNotFoundException - Handles any ClassNotFoundException
   @throws edu.ksu.cis.macr.obaa_pp.actuator.ActuatorOutOfServiceException - Handles any ActuatorOutOfService Exception
   @throws edu.ksu.cis.macr.obaa_pp.actuator.SettingOutofRangeException - Handles any SettingOutofRange Exception
   */
  @Override
  public int actAt(edu.ksu.cis.macr.obaa_pp.actuator.ISetting<?> setting, long timeSlice)
          throws IOException, ClassNotFoundException, edu.ksu.cis.macr.obaa_pp.actuator.ActuatorOutOfServiceException, edu.ksu.cis.macr.obaa_pp.actuator.SettingOutofRangeException {
    PhysicalSystemSimulator.actAt(this.getSmartInverterName(), timeSlice, setting);
    return 1;
  }



  private double calcQdirectoffset(final double q_load_now, final double p_load_now, final double p_gen_now, final double q_load_last, final double p_load_last, final double p_gen_last, final double q_gen_last) {
    final double q_gen_now = ((q_load_now - q_load_last) + ((p_load_now - p_load_last) - (p_gen_now - p_gen_last)) + q_gen_last);
    return q_gen_now;
  }

  /**
   Returns the reactive power setting using option 1: SmartInverterAlgorithm.FIXED_PF_INJECTION.

   @param phase - either "A", "B", or "C"
   @param now - the ElectricalData from the most recent sensor reading
   @param last - the ElectricalData from the previous sensor reading
   @return the new reactive power setting for this device.
   */
  @Override
  public double calculateReactivePower(String phase, IElectricalData now, IElectricalData last,
                                       double powerFactor, SmartInverterAlgorithm algorithm) {
    if (debug)
      LOG.debug("Calling CalculateReactivePower for phase {} with powerFactor = {} and algorithm={}. Now= {} and Last={}",
              phase, powerFactor, algorithm, now, last);
    // Delta Q = - Delta P
    // Solve for Q_gen_now (all others are known):
    //       (Q_load_now - Q_load_last)-(Q_gen_now - Q_gen_last) =
    // -1* ( (P_load_now - P_load_last)-(P_gen_now - P_gen_last) )
    double q_load_now = 0.0;
    double p_load_now = 0.0;
    double p_gen_now = 0.0;
    double q_load_last = 0.0;
    double p_load_last = 0.0;
    double p_gen_last = 0.0;
    double q_gen_last = 0.0;

    switch (phase) {
      case "A":
        q_load_now = now.getPhaseAQload();
        p_load_now = now.getPhaseAPload();
        p_gen_now = now.getPgeneration();
        q_load_last = last.getPhaseAQload();
        p_load_last = last.getPhaseAPload();
        p_gen_last = last.getPgeneration();
        q_gen_last = last.getQgeneration();
        break;
      case "B":
        q_load_now = now.getPhaseBQload();
        p_load_now = now.getPhaseBPload();
        p_gen_now = now.getPgeneration();
        q_load_last = last.getPhaseBQload();
        p_load_last = last.getPhaseBPload();
        p_gen_last = last.getPgeneration();
        q_gen_last = last.getQgeneration();
        break;
      case "C":
        q_load_now = now.getPhaseCQload();
        p_load_now = now.getPhaseCPload();
        p_gen_now = now.getPgeneration();
        q_load_last = last.getPhaseCQload();
        p_load_last = last.getPhaseCPload();
        p_gen_last = last.getPgeneration();
        q_gen_last = last.getQgeneration();
        break;
    }


    double q_gen_now = 0.0;
    if (algorithm == SmartInverterAlgorithm.FIXED_PF_BOUNDED_BY_MAXIMUM) {
      q_gen_now = getQ_gen_now(now, last, phase, powerFactor);
      //   q_gen_now = getQ_gen_now(q_load_now, p_load_now, p_gen_now, q_load_last, p_load_last, p_gen_last, q_gen_last, powerFactor);
    }

    if (debug)
      LOG.debug("Calc Q gen: = {} = ((q_load_now={} - q_load_last={}) + ((p_load_now={} - p_load_last={}) - (p_gen_now={}  - p_gen_last={})) + q_gen_last{});",
              q_gen_now, String.format("%.4f", q_load_now), String.format("%.4f", q_load_last), String.format("%.4f", p_load_now), String.format("%.4f", p_load_last), String.format("%.4f", p_gen_now), String.format("%.4f", p_gen_last), String.format("%.4f", q_gen_last));

    return q_gen_now;

  }

  /**
   Determine the reactive power setting based on the information given in the goal guidelines (what power factor and
   algorithm to use ) and the local @link{SmartMeterRead}:  this time slice's electrical data and  the
   previous timeslices electrical data. <p> Return true if any settings were updated; false if everything stays
   the same.

   @param smartMeterRead - the smart meter sensor reading containing information about current electrical data as well as
   previous electrical data.
   @param powerFactor - e.g. 0.75
   @param netDeltaP - the additional net delta P associated with the community request. A positive net delta P is a
   request for additional smart inverter reactive power.  A negative net delta P is a request to reduce the smart
   inverter reactive power.
   @return boolean - true if calculated; false if not
   */
  @Override
  public boolean calculateSmartInverterSetting(ISmartMeterRead smartMeterRead,
                                               final double powerFactor, long timeSlice, final double netDeltaP) {

    // get data readings for smart inverters
    // Delta Q = - Delta P
    // Solve for Q_gen_now (all others are known):
    //       (Q_load_now - Q_load_last)-(Q_gen_now - Q_gen_last) =
    // -1* ( (P_load_now - P_load_last)-(P_gen_now - P_gen_last) )

    // if we can't determine the algorithm, return

    SmartInverterAlgorithm algorithm = SmartInverterAlgorithm.FIXED_PF_BOUNDED_BY_MAXIMUM;
    if (algorithm == SmartInverterAlgorithm.NO_INJECTION || !RunManager.getUseLiveMatLab()) {
      return false;  // nothing to do
    }
    if (!(algorithm == SmartInverterAlgorithm.FIXED_PF_BOUNDED_BY_MAXIMUM)) {
      LOG.info("The guidelines say the injection algorithm is {}, this must be 1 or 2 to select a valid algorithm. Please check the simulation information.", algorithm);
      LOG.info("Set the injection algorithm to 0 for  no reactive power injection.");
      LOG.info("Set the injection algorithm to 1 to use the fixed power factor with a bounded maximum algorithm.");
      return false;
    }

    //   data

    IElectricalData now = smartMeterRead.getPreviousElectricalData();
    IElectricalData last = smartMeterRead.getElectricalData();

    //   if we're not generating, we can't inject so return

    if (now.getPgeneration() == 0.0) {
      if (debug) LOG.debug("This agent is not generating power, so there is no injection available.");
      return false;
    }

    // calculate the reactive power for appropriate phase (try all)

    double calculatedReactivePowerPhaseA = calculateReactivePower("A", now, last, powerFactor, algorithm);
    double calculatedReactivePowerPhaseB = calculateReactivePower("B", now, last, powerFactor, algorithm);
    double calculatedReactivePowerPhaseC = calculateReactivePower("C", now, last, powerFactor, algorithm);

    // report for all three phases, each home is only on one phase

    if (debug) LOG.debug("Calculated reactive power for {}:\tPhaseA = {}\tPhaseB = {}\tPhaseC = {}",
            this.smartInverterName, calculatedReactivePowerPhaseA, calculatedReactivePowerPhaseB, calculatedReactivePowerPhaseC);
    if (calculatedReactivePowerPhaseA != 0.0) {
      now.setQgeneration_calculated(calculatedReactivePowerPhaseA);
    }
    if (calculatedReactivePowerPhaseB != 0.0) {
      now.setQgeneration_calculated(calculatedReactivePowerPhaseB);
    }
    if (calculatedReactivePowerPhaseC != 0.0) {
      now.setQgeneration_calculated(calculatedReactivePowerPhaseC);
    }

    // issue the control action to the Physical System Simulator

    try {

      double newValue = now.getQgeneration_calculated();
      String strValue = String.format("%s", newValue);

      // create the custom setting object
      SmartInverterSetting smartInverterSetting = new SmartInverterSetting();
      smartInverterSetting.setQ_gen_now(newValue);
      smartInverterSetting.setSmartInverterName(this.getSmartInverterName());
      smartInverterSetting.setSmartMeterName(this.getSmartMeterNameFromSmartInverterName(this.getSmartInverterName()));
      smartInverterSetting.setTimeSlice(timeSlice);

      // create the generic version for use in standard actuator method signatures
      edu.ksu.cis.macr.obaa_pp.actuator.Setting<ActuatorType> setting = new edu.ksu.cis.macr.obaa_pp.actuator.Setting<>();
      setting.setActuatorType(ActuatorType.SmartInverter);
      setting.setActuatorSettingObject(smartInverterSetting);

      LOG.info("ISSUING CONTROL ACTION at time slice {}: Set Smart Inverter {} reactive power TO {}",
              timeSlice, this.smartInverterName, strValue);
      PhysicalSystemSimulator.actAt(smartInverterName, timeSlice, setting);

    } catch (Exception e) {
      LOG.error("ERROR: could not issue control action for updated q {}   {}   {}",
              smartInverterName, timeSlice, now.toString());
      System.exit(-2);
    }


    // notify the Run Manager that I've completed my timeSlice evaluation

    String me = this.smartInverterName;
    boolean isLast = reportingSmartInverterControlAction(timeSlice, me);
    return isLast;

      //TODO: remove isLast from SmartInverterCapability
  }


//    q_load_now - Q load this time slice
//    * @param q_load_last - Q load last time slice
//    * @param p_load_now - P load this time slice
//    * @param p_load_last - P load last time slice
//    * @param p_gen_now - P generation this time slice
//    * @param p_gen_last - P generation last time slice
//    * @param q_gen_last - Q generation this time slice

  @Override
  public double getFailure() {
    return 0;
  }

  /**
   Get the new reactive power setting given the power factor provided in the run.properties file.

   @param now - the most recent set of Electrical Data for the associated sensor
   @param last - the previous set of Electrical Data for the associated sensor
   @param phase - the electrical power phase (we're a 3-phase system - it's either A, B, or C)
   @return - the desired reactive power setting (Q generation at time now)
   */
  public double getQ_gen_now(IElectricalData now, IElectricalData last, String phase) {
    return getQ_gen_now(now, last, phase, RunManager.getPowerFactor());

  }

  /**
   Calculate Q generation now from the last time slice and the load P, the load Q, and the generation P for this time
   slice.  Delta Q = - Delta P Solve for Q_gen_now (all others are known): (Q_load_now - Q_load_last)-(Q_gen_now -
   Q_gen_last) = -1* ( (P_load_now - P_load_last)-(P_gen_now - P_gen_last) )     /** Get the new reactive
   power setting for a given power factor (for testing power factor response).

   @param now - the most recent set of Electrical Data for the associated sensor
   @param last - the previous set of Electrical Data for the associated sensor
   @param phase - the electrical power phase (we're a 3-phase system - it's either A, B, or C)
   @param powerFactor - the power factor to use, generally about 0.75.
   @return double - the new reactive power setting for this inverter that directly offsets the net generation at this
   device.
   */
  public double getQ_gen_now(IElectricalData now, IElectricalData last, String phase, final double powerFactor) {

    double Qmax;            //Max allowed Reactive power generated (KW)
    final double P1 = 0.6525;     //This is used to calculate Qmax (KW)

    if (now.getPgeneration() >= P1) {
      final double Smax = 0.87;    //This has units in KW
      Qmax = Math.sqrt(Smax * Smax + (-1) * now.getPgeneration() * now.getPgeneration());
      LOG.info("Qmax Option 1 (if pgen >= Limit) = {}. Pgen={} and Limit = {} = Pgen at P1 (a constant)", Qmax, now.getPgeneration(), P1);
    } else {
      Qmax = now.getPgeneration() * Math.tan(Math.acos(powerFactor));
      LOG.info("Qmax Opt 2 pgen<Limit={}. Pgen={}, Limit={}, Qmax=Pgen*Math.tan(Math.acos(PF) where PF={},C={}", Qmax, now.getPgeneration(), P1, powerFactor, Math.tan(Math.acos(powerFactor)));
    }

    double q_gen_now = 0.0;
    if (phase.equals("A")) {
      q_gen_now = (-1) * now.getPgeneration() + last.getPgeneration() + now.getPhaseAPload
              () + (-1) * last.getPhaseAPload() + now.getPhaseAQload() + (-1) * last
              .getPhaseAQload() + last.getQgeneration();
    }
    if (phase.equals("B")) {
      q_gen_now = (-1) * now.getPgeneration() + last.getPgeneration() + now.getPhaseBPload
              () + (-1) * last.getPhaseBPload() + now.getPhaseBQload() + (-1) * last.getPhaseBQload() + last.getQgeneration();
    }
    if (phase.equals("C")) {
      q_gen_now = (-1) * now.getPgeneration() + last.getPgeneration() + now.getPhaseCPload
              () + (-1) * last.getPhaseCPload() + now.getPhaseCQload() + (-1) * last.getPhaseCQload() + last.getQgeneration();
    }

    double result = 0.0;
    if (Qmax < q_gen_now) {
      result = Qmax;
    } else {
      result = q_gen_now;
    }
    LOG.info("Returning Qmax={}.  UnconstrainedQ = {}, Qmax = {} ", result, q_gen_now, Qmax);
    return result;
  }

  public String getSmartInverterName() {
    return this.smartInverterName;
  }

  /**
   @param smartInverterName - the unique name of the smart inverter to read
   */
  public synchronized void setSmartInverterName(final String smartInverterName) {
    this.smartInverterName = smartInverterName;

  }

  private String getSmartInverterNameFromSmartMeterName(final String smartMeterName) {
    return smartMeterName.replace('M', 'I');
  }

  private String getSmartMeterNameFromSmartInverterName(final String smartInverterName) {
    return smartInverterName.replace('I', 'M');
  }

  /**
   Get the parameters from this instance goal and use them to initialize the capability.

   @param instanceGoal - this instance of the specification goal
   */
  public synchronized void initializeFromGoal(InstanceGoal<?> instanceGoal) {
    if (debug) LOG.debug("Initializing capability from goal: {}.", instanceGoal);
    // Get the parameter values from the existing active instance goal
    final InstanceParameters params = Objects
            .requireNonNull((InstanceParameters) instanceGoal
                    .getParameter());
    if (debug) LOG.debug("Initializing params: {}.", params);
  }


  @Override
  public synchronized void reset() {
  }

  @Override
  public Element toElement(final Document document) {
    final Element capability = super.toElement(document);
    Element parameter = (Element) capability.appendChild(document
            .createElement(ELEMENT_PARAMETER));
    parameter.setAttribute(ATTRIBUTE_TYPE,
            int.class.getSimpleName());
    parameter.appendChild(document.createTextNode(this.smartInverterName));
    return capability;
  }

    @Override
    public String toString() {
        return "SmartInverterCapability{" +
                "owner=" + owner +
                ", smartInverterName='" + smartInverterName + '\'' +
                '}';
    }
}
