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

package com.sbdc.loggerhead;

import com.sbdc.loggerhead.compoundlogger.CompoundLogger;
import com.sbdc.loggerhead.primarylogger.AbstractPrimaryLog;
import com.sbdc.loggerhead.primarylogger.PrimaryBooleanLog;
import com.sbdc.loggerhead.primarylogger.PrimaryDoubleLog;
import com.sbdc.loggerhead.primarylogger.PrimaryIntegerLog;
import com.sbdc.loggerhead.primarylogger.PrimaryPoseLog;
import com.sbdc.loggerhead.primarylogger.PrimaryStringLog;
import com.sbdc.loggerhead.primarylogger.PrimarySwerveStateLog;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.util.datalog.DataLog;
import edu.wpi.first.wpilibj.DataLogManager;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.livewindow.LiveWindow;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

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

    public void update() {
      log.update(newValue.get());
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

  private final List<SourceUpdateMap<PrimaryStringLog, String>> stringLogs = new ArrayList<>();
  private final List<SourceUpdateMap<PrimaryDoubleLog, Double>> doubleLogs = new ArrayList<>();
  private final List<SourceUpdateMap<PrimaryIntegerLog, Integer>> integerLogs = new ArrayList<>();
  private final List<SourceUpdateMap<PrimaryBooleanLog, Boolean>> booleanLogs = new ArrayList<>();
  private final List<SourceUpdateMap<PrimaryPoseLog, Pose2d>> poseLogs = new ArrayList<>();
  private final List<SourceUpdateMap<PrimarySwerveStateLog, SwerveModuleState[]>> swerveStateLogs =
      new ArrayList<>();

  private final List<CompoundLogger> compoundLoggers = new ArrayList<>();

  private final Table rootTable = new Table(this);

  private Loggerhead() {}

  /**
   * Initializes logging framework by starting DataLogManager, disabling default logging of
   * NetworkTables, and hooking up the driver station logging
   */
  public void initializeLogging() {
    DataLogManager.logNetworkTables(false);
    DataLogManager.start();
    LiveWindow.disableAllTelemetry();
    DriverStation.startDataLog(DataLogManager.getLog());

    configurator.getConfiguratorCallback().run();
  }

  public void initializeLogging(boolean runsConfigureCallback) {
    DataLogManager.logNetworkTables(false);
    DataLogManager.start();
    LiveWindow.disableAllTelemetry();
    DriverStation.startDataLog(DataLogManager.getLog());

    if (runsConfigureCallback) {
      configurator.getConfiguratorCallback().run();
    }
  }

  public Table getRootTable() {
    return rootTable;
  }

  public Loggerhead addStringLogger(String key, LogMode mode, Supplier<String> stringGetter) {
    PrimaryStringLog logPub = new PrimaryStringLog(key, mode, ntInst, log);
    SourceUpdateMap<PrimaryStringLog, String> compundLogger =
        new SourceUpdateMap<>(this, logPub, stringGetter);
    stringLogs.add(compundLogger);

    return this;
  }

  public Loggerhead addDoubleLogger(String key, LogMode mode, Supplier<Double> doubleGetter) {
    PrimaryDoubleLog logPub = new PrimaryDoubleLog(key, mode, ntInst, log);
    SourceUpdateMap<PrimaryDoubleLog, Double> compundLogger =
        new SourceUpdateMap<>(this, logPub, doubleGetter);
    doubleLogs.add(compundLogger);

    return this;
  }

  public Loggerhead addIntegerLogger(String key, LogMode mode, Supplier<Integer> doubleGetter) {
    PrimaryIntegerLog logPub = new PrimaryIntegerLog(key, mode, ntInst, log);
    SourceUpdateMap<PrimaryIntegerLog, Integer> compundLogger =
        new SourceUpdateMap<>(this, logPub, doubleGetter);
    integerLogs.add(compundLogger);

    return this;
  }

  public Loggerhead addBooleanLogger(String key, LogMode mode, Supplier<Boolean> boolGetter) {
    PrimaryBooleanLog logPub = new PrimaryBooleanLog(key, mode, ntInst, log);
    SourceUpdateMap<PrimaryBooleanLog, Boolean> compundLogger =
        new SourceUpdateMap<>(this, logPub, boolGetter);
    booleanLogs.add(compundLogger);

    return this;
  }

  public Loggerhead addPoseLogger(String key, LogMode mode, Supplier<Pose2d> poseGetter) {
    PrimaryPoseLog logPub = new PrimaryPoseLog(key, mode, ntInst, log);
    SourceUpdateMap<PrimaryPoseLog, Pose2d> compundLogger =
        new SourceUpdateMap<>(this, logPub, poseGetter);
    poseLogs.add(compundLogger);

    return this;
  }

  public Loggerhead addSwerveStateLogger(
      String key, LogMode mode, Supplier<SwerveModuleState[]> moduleStateGetter) {
    PrimarySwerveStateLog logPub = new PrimarySwerveStateLog(key, mode, ntInst, log);
    SourceUpdateMap<PrimarySwerveStateLog, SwerveModuleState[]> compundLogger =
        new SourceUpdateMap<>(this, logPub, moduleStateGetter);
    swerveStateLogs.add(compundLogger);

    return this;
  }

  /**
   * Add a compound logger to this Loggerhead instance. Note the compound logger must be initalized.
   *
   * @param compoundLogger the CompoundLogger
   * @return Loggerhead instance for chaining
   */
  public Loggerhead addCompoundLogger(CompoundLogger compoundLogger) {
    compoundLoggers.add(compoundLogger);
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
    stringLogs.clear();
    doubleLogs.clear();
    integerLogs.clear();
    booleanLogs.clear();
    poseLogs.clear();
    swerveStateLogs.clear();
    compoundLoggers.clear();

    rootTable.clearSubtables();

    // ntInst.stopLocal();
    // ntInst.startServer();
    // TODO figure out how to change logs
    // DataLogManager.stop();
    // DataLogManager.start(null, null);
  }

  /**
   * Update all loggers. Must be called often, such as every robotPeriodic, usually 20 milliseconds.
   */
  public void update() {
    if (DriverStation.isDisabled()) {
      configurator.checkAllHooks();
    }

    stringLogs.forEach(SourceUpdateMap::update);
    doubleLogs.forEach(SourceUpdateMap::update);
    integerLogs.forEach(SourceUpdateMap::update);
    booleanLogs.forEach(SourceUpdateMap::update);
    poseLogs.forEach(SourceUpdateMap::update);
    swerveStateLogs.forEach(SourceUpdateMap::update);
    compoundLoggers.forEach(CompoundLogger::update);
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
}
