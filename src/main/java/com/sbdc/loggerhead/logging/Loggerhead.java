/*
 Copyright (C) 2026  Evan Hansen

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU Affero General Public License as published
 by the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License
 along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/

package com.sbdc.loggerhead.logging;

import com.sbdc.loggerhead.logging.compoundlogger.CompoundLogger;
import com.sbdc.loggerhead.logging.primarylogger.AbstractPrimaryLog;
import com.sbdc.loggerhead.logging.primarylogger.PrimaryBooleanArrayLog;
import com.sbdc.loggerhead.logging.primarylogger.PrimaryBooleanLog;
import com.sbdc.loggerhead.logging.primarylogger.PrimaryDoubleArrayLog;
import com.sbdc.loggerhead.logging.primarylogger.PrimaryDoubleLog;
import com.sbdc.loggerhead.logging.primarylogger.PrimaryIntegerArrayLog;
import com.sbdc.loggerhead.logging.primarylogger.PrimaryIntegerLog;
import com.sbdc.loggerhead.logging.primarylogger.PrimaryStringArrayLog;
import com.sbdc.loggerhead.logging.primarylogger.PrimaryStringLog;
import com.sbdc.loggerhead.logging.primarylogger.PrimaryStructArrayLog;
import com.sbdc.loggerhead.logging.primarylogger.PrimaryStructLog;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.Publisher;
import edu.wpi.first.util.datalog.DataLog;
import edu.wpi.first.util.datalog.DataLogEntry;
import edu.wpi.first.util.struct.Struct;
import edu.wpi.first.wpilibj.DataLogManager;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.livewindow.LiveWindow;
import java.util.HashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.management.RuntimeErrorException;

/** Singleton that orchestrates all logging. The entry-point of the library */
public class Loggerhead {
  // TODO: Name this better
  public static class SourceUpdateMap<T extends AbstractPrimaryLog<E, ?, ?>, E> {
    public final T log;
    public final Supplier<E> newValue;
    public final Loggerhead root;

    public SourceUpdateMap(Loggerhead root, T log, Supplier<E> newValue) {
      this.root = root;
      this.log = log;
      this.newValue = newValue;
    }

    public void updateFromAutoSource() {
      log.update(newValue.get());
    }

    public void updateManual(E newValue) {

    }
  }

  private static final Loggerhead INSTANCE = new Loggerhead();

  /**
   * @return Loggerhead instance
   */
  public static Loggerhead getInstance() {
    return INSTANCE;
  }

  private final NetworkTableInstance ntInst = NetworkTableInstance.getDefault();
  private final DataLog log = DataLogManager.getLog();
  private final Configurator configurator = new Configurator(this::cleanLoggers, () -> {});

  private final HashMap<String, SourceUpdateMap<?, ?>> autoPrimaryLogs = new HashMap<>();
  private final HashMap<String, AbstractPrimaryLog<?, ?, ?>> manualPrimaryLogs = new HashMap<>();
  private final HashMap<String, CompoundLogger> compoundLoggers = new HashMap<>();

  private final Table rootTable = new Table(this);

  private Loggerhead() {}

  /**
   * Pasthrough function for {@code Loggerhead.getInstance().getConfigurator().addHook} Adds a hook
   * to the configurator using hookValuegetter as the input.
   *
   * @param hookValueGetter supplier of a value
   */
  public void addHook(Supplier<?> hookValueGetter) {
    configurator.addHook(hookValueGetter);
  }

  /**
   * Pasthrough function for {@code Loggerhead.getInstance().getConfigurator().setConfigureCallback}
   *
   * <p>Set the user configuration function
   *
   * @param configureCallback
   */
  public void setConfigureCallback(Runnable configureCallback) {
    configurator.setConfigureCallback(configureCallback);
  }

  private void commonDefaultInitialize() {
    DataLogManager.logNetworkTables(false);
    DataLogManager.start();
    LiveWindow.disableAllTelemetry();
    DriverStation.startDataLog(DataLogManager.getLog());
  }

  /**
   * Initializes logging framework by starting DataLogManager, disabling default logging of
   * NetworkTables, and hooking up the driver station logging
   */
  public void initializeLogging() {
    commonDefaultInitialize();

    configurator.getConfiguratorCallback().run();
  }

  /**
   * Initializes logging framework by starting DataLogManager, disabling default logging of
   * NetworkTables, and hooking up the driver station logging
   *
   * @param runsConfigureCallback wether or not to run the configure callback automatically
   */
  public void initializeLogging(boolean runsConfigureCallback) {
    commonDefaultInitialize();

    if (runsConfigureCallback) {
      configurator.getConfiguratorCallback().run();
    }
  }

  public Table getRootTable() {
    return rootTable;
  }

  /**
   * Add a compound logger to this Loggerhead instance. Note the compound logger must be initalized.
   *
   * @param compoundLogger the CompoundLogger
   * @return Loggerhead instance for chaining
   */
  public Loggerhead addCompoundLogger(CompoundLogger compoundLogger) {
    compoundLoggers.put(compoundLogger.getName(), compoundLogger);
    return this;
  }

  // --- Configuration ---

  /**
   * @param applyTo callable that recieves the {@link Configurator}
   * @return Loggerhead instance for chaining
   */
  public Loggerhead applyToConfigurator(Consumer<Configurator> applyTo) {
    applyTo.accept(configurator);
    return this;
  }

  public Configurator getConfigurator() {
    return configurator;
  }

  /**
   * Clean all primary logger objects, compound logger objects, and tables. Primary use is
   * reconfiguration. Called automatically by {@link Configurator} every configuration
   */
  public void cleanLoggers() {
    autoPrimaryLogs.clear();
    compoundLoggers.clear();

    rootTable.clearSubtables();

    // ntInst.stopLocal();
    // ntInst.startServer();
    // TODO figure out how to change logs
    // DataLogManager.stop();
    // DataLogManager.start(null, null);
  }

  /**
   * Update all loggers. Must be called often, such as every robotPeriodic, almost always 20
   * milliseconds.
   */
  public void update() {
    if (DriverStation.isDisabled()) {
      configurator.checkAllHooks();
    }

    autoPrimaryLogs.forEach((key, srcupdatemap) -> srcupdatemap.updateFromAutoSource());
    compoundLoggers.forEach((key, srcupdatemap) -> srcupdatemap.update());
  }

  /**
   * @return The DataLog instance Loggerhead is using
   */
  public DataLog getDataLog() {
    return log;
  }

  /**
   * @return The NetworkTable instance Loggerhead is using
   */
  public NetworkTableInstance getNetworkTableInstance() {
    return ntInst;
  }




// LGH BEGIN GENERATED 596a96cc7bf9108cd896f33c44aedc8a

/** * Add a Boolean logger to the Loggerhead instance
 * 
 * @param key Name of the string logger without slashes
 * @param mode Logging mode for the string logger
 * @param booleanGetter Callable providing the string*/


public void addBooleanLogger( String key, LogMode mode, Supplier<Boolean> booleanGetter) { 

PrimaryBooleanLog logPub = new PrimaryBooleanLog(key, mode, ntInst, log);
SourceUpdateMap<PrimaryBooleanLog, Boolean> compundLogger =
    new SourceUpdateMap<>(this, logPub, booleanGetter);
autoPrimaryLogs.put(key, compundLogger);
 
 }

/** * Add a String logger to the Loggerhead instance
 * 
 * @param key Name of the string logger without slashes
 * @param mode Logging mode for the string logger
 * @param stringGetter Callable providing the string*/


public void addStringLogger( String key, LogMode mode, Supplier<String> stringGetter) { 

PrimaryStringLog logPub = new PrimaryStringLog(key, mode, ntInst, log);
SourceUpdateMap<PrimaryStringLog, String> compundLogger =
    new SourceUpdateMap<>(this, logPub, stringGetter);
autoPrimaryLogs.put(key, compundLogger);
 
 }

/** * Add a Integer logger to the Loggerhead instance
 * 
 * @param key Name of the string logger without slashes
 * @param mode Logging mode for the string logger
 * @param integerGetter Callable providing the string*/


public void addIntegerLogger( String key, LogMode mode, Supplier<Integer> integerGetter) { 

PrimaryIntegerLog logPub = new PrimaryIntegerLog(key, mode, ntInst, log);
SourceUpdateMap<PrimaryIntegerLog, Integer> compundLogger =
    new SourceUpdateMap<>(this, logPub, integerGetter);
autoPrimaryLogs.put(key, compundLogger);
 
 }

/** * Add a Double logger to the Loggerhead instance
 * 
 * @param key Name of the string logger without slashes
 * @param mode Logging mode for the string logger
 * @param doubleGetter Callable providing the string*/


public void addDoubleLogger( String key, LogMode mode, Supplier<Double> doubleGetter) { 

PrimaryDoubleLog logPub = new PrimaryDoubleLog(key, mode, ntInst, log);
SourceUpdateMap<PrimaryDoubleLog, Double> compundLogger =
    new SourceUpdateMap<>(this, logPub, doubleGetter);
autoPrimaryLogs.put(key, compundLogger);
 
 }

/** * Add a BooleanArray logger to the Loggerhead instance
 * 
 * @param key Name of the string logger without slashes
 * @param mode Logging mode for the string logger
 * @param booleanArrayGetter Callable providing the string*/


public void addBooleanArrayLogger( String key, LogMode mode, Supplier<boolean[]> booleanArrayGetter) { 

PrimaryBooleanArrayLog logPub = new PrimaryBooleanArrayLog(key, mode, ntInst, log);
SourceUpdateMap<PrimaryBooleanArrayLog, boolean[]> compundLogger =
    new SourceUpdateMap<>(this, logPub, booleanArrayGetter);
autoPrimaryLogs.put(key, compundLogger);
 
 }

/** * Add a StringArray logger to the Loggerhead instance
 * 
 * @param key Name of the string logger without slashes
 * @param mode Logging mode for the string logger
 * @param stringArrayGetter Callable providing the string*/


public void addStringArrayLogger( String key, LogMode mode, Supplier<String[]> stringArrayGetter) { 

PrimaryStringArrayLog logPub = new PrimaryStringArrayLog(key, mode, ntInst, log);
SourceUpdateMap<PrimaryStringArrayLog, String[]> compundLogger =
    new SourceUpdateMap<>(this, logPub, stringArrayGetter);
autoPrimaryLogs.put(key, compundLogger);
 
 }

/** * Add a IntegerArray logger to the Loggerhead instance
 * 
 * @param key Name of the string logger without slashes
 * @param mode Logging mode for the string logger
 * @param integerArrayGetter Callable providing the string*/


public void addIntegerArrayLogger( String key, LogMode mode, Supplier<long[]> integerArrayGetter) { 

PrimaryIntegerArrayLog logPub = new PrimaryIntegerArrayLog(key, mode, ntInst, log);
SourceUpdateMap<PrimaryIntegerArrayLog, long[]> compundLogger =
    new SourceUpdateMap<>(this, logPub, integerArrayGetter);
autoPrimaryLogs.put(key, compundLogger);
 
 }

/** * Add a DoubleArray logger to the Loggerhead instance
 * 
 * @param key Name of the string logger without slashes
 * @param mode Logging mode for the string logger
 * @param doubleArrayGetter Callable providing the string*/


public void addDoubleArrayLogger( String key, LogMode mode, Supplier<double[]> doubleArrayGetter) { 

PrimaryDoubleArrayLog logPub = new PrimaryDoubleArrayLog(key, mode, ntInst, log);
SourceUpdateMap<PrimaryDoubleArrayLog, double[]> compundLogger =
    new SourceUpdateMap<>(this, logPub, doubleArrayGetter);
autoPrimaryLogs.put(key, compundLogger);
 
 }

/** * Add a Boolean logger to the Loggerhead instance
 * 
 * @param key Name of the string logger without slashes
 * @param mode Logging mode for the string logger
 * @param booleanGetter Callable providing the string*/


public void manualPutBoolean( String key, LogMode mode, Boolean newBoolean) { 

if (autoPrimaryLogs.containsKey(key)) {
      throw new RuntimeException("Manual and automatic loggers cannot have the same path/name");
    }

    PrimaryBooleanLog logPub;
    if (manualPrimaryLogs.containsKey(key)) {
      if (manualPrimaryLogs.get(key) instanceof PrimaryBooleanLog) {
        logPub = (PrimaryBooleanLog) manualPrimaryLogs.get(key);
      } else {
        throw new RuntimeException("Logger: " + key + ", is not a boolean logger but a boolean value was attemped to be published");
      }
    } else {
      logPub = new PrimaryBooleanLog(key, mode, ntInst, log);
      manualPrimaryLogs.put(key, logPub);
    }

  logPub.update(newBoolean);
 
 }

/** * Add a String logger to the Loggerhead instance
 * 
 * @param key Name of the string logger without slashes
 * @param mode Logging mode for the string logger
 * @param stringGetter Callable providing the string*/


public void manualPutString( String key, LogMode mode, String newString) { 

if (autoPrimaryLogs.containsKey(key)) {
      throw new RuntimeException("Manual and automatic loggers cannot have the same path/name");
    }

    PrimaryStringLog logPub;
    if (manualPrimaryLogs.containsKey(key)) {
      if (manualPrimaryLogs.get(key) instanceof PrimaryStringLog) {
        logPub = (PrimaryStringLog) manualPrimaryLogs.get(key);
      } else {
        throw new RuntimeException("Logger: " + key + ", is not a boolean logger but a boolean value was attemped to be published");
      }
    } else {
      logPub = new PrimaryStringLog(key, mode, ntInst, log);
      manualPrimaryLogs.put(key, logPub);
    }

  logPub.update(newString);
 
 }

/** * Add a Integer logger to the Loggerhead instance
 * 
 * @param key Name of the string logger without slashes
 * @param mode Logging mode for the string logger
 * @param integerGetter Callable providing the string*/


public void manualPutInteger( String key, LogMode mode, Integer newInteger) { 

if (autoPrimaryLogs.containsKey(key)) {
      throw new RuntimeException("Manual and automatic loggers cannot have the same path/name");
    }

    PrimaryIntegerLog logPub;
    if (manualPrimaryLogs.containsKey(key)) {
      if (manualPrimaryLogs.get(key) instanceof PrimaryIntegerLog) {
        logPub = (PrimaryIntegerLog) manualPrimaryLogs.get(key);
      } else {
        throw new RuntimeException("Logger: " + key + ", is not a boolean logger but a boolean value was attemped to be published");
      }
    } else {
      logPub = new PrimaryIntegerLog(key, mode, ntInst, log);
      manualPrimaryLogs.put(key, logPub);
    }

  logPub.update(newInteger);
 
 }

/** * Add a Double logger to the Loggerhead instance
 * 
 * @param key Name of the string logger without slashes
 * @param mode Logging mode for the string logger
 * @param doubleGetter Callable providing the string*/


public void manualPutDouble( String key, LogMode mode, Double newDouble) { 

if (autoPrimaryLogs.containsKey(key)) {
      throw new RuntimeException("Manual and automatic loggers cannot have the same path/name");
    }

    PrimaryDoubleLog logPub;
    if (manualPrimaryLogs.containsKey(key)) {
      if (manualPrimaryLogs.get(key) instanceof PrimaryDoubleLog) {
        logPub = (PrimaryDoubleLog) manualPrimaryLogs.get(key);
      } else {
        throw new RuntimeException("Logger: " + key + ", is not a boolean logger but a boolean value was attemped to be published");
      }
    } else {
      logPub = new PrimaryDoubleLog(key, mode, ntInst, log);
      manualPrimaryLogs.put(key, logPub);
    }

  logPub.update(newDouble);
 
 }

/** * Add a BooleanArray logger to the Loggerhead instance
 * 
 * @param key Name of the string logger without slashes
 * @param mode Logging mode for the string logger
 * @param booleanArrayGetter Callable providing the string*/


public void manualPutBooleanArray( String key, LogMode mode, boolean[] newBooleanArray) { 

if (autoPrimaryLogs.containsKey(key)) {
      throw new RuntimeException("Manual and automatic loggers cannot have the same path/name");
    }

    PrimaryBooleanArrayLog logPub;
    if (manualPrimaryLogs.containsKey(key)) {
      if (manualPrimaryLogs.get(key) instanceof PrimaryBooleanArrayLog) {
        logPub = (PrimaryBooleanArrayLog) manualPrimaryLogs.get(key);
      } else {
        throw new RuntimeException("Logger: " + key + ", is not a boolean logger but a boolean value was attemped to be published");
      }
    } else {
      logPub = new PrimaryBooleanArrayLog(key, mode, ntInst, log);
      manualPrimaryLogs.put(key, logPub);
    }

  logPub.update(newBooleanArray);
 
 }

/** * Add a StringArray logger to the Loggerhead instance
 * 
 * @param key Name of the string logger without slashes
 * @param mode Logging mode for the string logger
 * @param stringArrayGetter Callable providing the string*/


public void manualPutStringArray( String key, LogMode mode, String[] newStringArray) { 

if (autoPrimaryLogs.containsKey(key)) {
      throw new RuntimeException("Manual and automatic loggers cannot have the same path/name");
    }

    PrimaryStringArrayLog logPub;
    if (manualPrimaryLogs.containsKey(key)) {
      if (manualPrimaryLogs.get(key) instanceof PrimaryStringArrayLog) {
        logPub = (PrimaryStringArrayLog) manualPrimaryLogs.get(key);
      } else {
        throw new RuntimeException("Logger: " + key + ", is not a boolean logger but a boolean value was attemped to be published");
      }
    } else {
      logPub = new PrimaryStringArrayLog(key, mode, ntInst, log);
      manualPrimaryLogs.put(key, logPub);
    }

  logPub.update(newStringArray);
 
 }

/** * Add a IntegerArray logger to the Loggerhead instance
 * 
 * @param key Name of the string logger without slashes
 * @param mode Logging mode for the string logger
 * @param integerArrayGetter Callable providing the string*/


public void manualPutIntegerArray( String key, LogMode mode, long[] newIntegerArray) { 

if (autoPrimaryLogs.containsKey(key)) {
      throw new RuntimeException("Manual and automatic loggers cannot have the same path/name");
    }

    PrimaryIntegerArrayLog logPub;
    if (manualPrimaryLogs.containsKey(key)) {
      if (manualPrimaryLogs.get(key) instanceof PrimaryIntegerArrayLog) {
        logPub = (PrimaryIntegerArrayLog) manualPrimaryLogs.get(key);
      } else {
        throw new RuntimeException("Logger: " + key + ", is not a boolean logger but a boolean value was attemped to be published");
      }
    } else {
      logPub = new PrimaryIntegerArrayLog(key, mode, ntInst, log);
      manualPrimaryLogs.put(key, logPub);
    }

  logPub.update(newIntegerArray);
 
 }

/** * Add a DoubleArray logger to the Loggerhead instance
 * 
 * @param key Name of the string logger without slashes
 * @param mode Logging mode for the string logger
 * @param doubleArrayGetter Callable providing the string*/


public void manualPutDoubleArray( String key, LogMode mode, double[] newDoubleArray) { 

if (autoPrimaryLogs.containsKey(key)) {
      throw new RuntimeException("Manual and automatic loggers cannot have the same path/name");
    }

    PrimaryDoubleArrayLog logPub;
    if (manualPrimaryLogs.containsKey(key)) {
      if (manualPrimaryLogs.get(key) instanceof PrimaryDoubleArrayLog) {
        logPub = (PrimaryDoubleArrayLog) manualPrimaryLogs.get(key);
      } else {
        throw new RuntimeException("Logger: " + key + ", is not a boolean logger but a boolean value was attemped to be published");
      }
    } else {
      logPub = new PrimaryDoubleArrayLog(key, mode, ntInst, log);
      manualPrimaryLogs.put(key, logPub);
    }

  logPub.update(newDoubleArray);
 
 }
public <T, S extends Struct<T>> Loggerhead addStructLogger(
String key, LogMode mode, Supplier<T> moduleStateGetter, Struct<T> struct) {
PrimaryStructLog<T, S> logPub = new PrimaryStructLog<>(key, mode, ntInst, log, struct);
SourceUpdateMap<PrimaryStructLog<T, S>, T> compundLogger =
    new SourceUpdateMap<>(this, logPub, moduleStateGetter);
autoPrimaryLogs.put(key, compundLogger);

return this;
}

public <T, S extends Struct<T>> Loggerhead addStructArrayLogger(
  String key, LogMode mode, Supplier<T[]> valueGetter, Struct<T> struct) {
PrimaryStructArrayLog<T, S> logPub =
    new PrimaryStructArrayLog<>(key, mode, ntInst, log, struct);
SourceUpdateMap<PrimaryStructArrayLog<T, S>, T[]> mapping =
    new SourceUpdateMap<>(this, logPub, valueGetter);
autoPrimaryLogs.put(key, mapping);
return this;
}

public <T, S extends Struct<T>> void manualPutStruct(String key, LogMode mode, T value, Struct<T> struct) {
    if (autoPrimaryLogs.containsKey(key)) {
      throw new RuntimeException("Manual and automatic loggers cannot have the same path/name");
    }

    PrimaryStructLog<T, S> logPub;
    if (manualPrimaryLogs.containsKey(key)) {
      manualPrimaryLogs.get(key);
      if (manualPrimaryLogs.get(key) instanceof PrimaryStructLog<T, S>) {
        logPub = (PrimaryStructLog<T, S>) manualPrimaryLogs.get(key);
      } else {
        throw new RuntimeException("Logger: " + key + ", is not a boolean logger but a boolean value was attemped to be published");
      }
    } else {
      logPub = new PrimaryStructLog<T, S>(key, mode, ntInst, log, struct);
      manualPrimaryLogs.put(key, logPub);
    }

  logPub.update(value);
  }

// LGH END GENERATED 596a96cc7bf9108cd896f33c44aedc8a
}
