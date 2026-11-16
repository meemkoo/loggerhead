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

package com.sbdc.loggerhead.core;

import static edu.wpi.first.units.Units.Amps;
import static edu.wpi.first.units.Units.Degrees;
import static edu.wpi.first.units.Units.Meters;
import static edu.wpi.first.units.Units.RPM;

import com.sbdc.loggerhead.core.Integration.PresentLibs;
import com.sbdc.loggerhead.logging.LogMode;
import com.sbdc.loggerhead.logging.Loggerhead;
import com.sbdc.loggerhead.logging.Table;
import edu.wpi.first.units.measure.Angle;
import edu.wpi.first.units.measure.Distance;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj2.command.Command;
import java.util.EnumSet;
import java.util.HashMap;
import yams.motorcontrollers.SmartMotorController;

public class LoggerheadUtils {
  public enum DefaultSMCLoggingMode {
    PositionalAngular,
    PositionalLinear,
    SpeedAngular;
  }

  public static void warnIfDriverStationOutOfTest(String message) {
    String callerMethod =
        StackWalker.getInstance()
            .walk(
                stream ->
                    stream
                        .skip(1)
                        .findFirst()
                        .map(StackWalker.StackFrame::getMethodName)
                        .orElse("Unknown"));

    if (!DriverStation.isTest()) {
      DriverStation.reportWarning(
          "Test method called out of test mode! Method: " + callerMethod + " Message:" + message,
          false);
    }
  }

  public static void warnIfDriverStationOutOfTest() {
    String callerMethod =
        StackWalker.getInstance()
            .walk(
                stream ->
                    stream
                        .skip(1)
                        .findFirst()
                        .map(StackWalker.StackFrame::getMethodName)
                        .orElse("Unknown"));

    if (!DriverStation.isTest()) {
      DriverStation.reportWarning(
          "Test method called out of test mode! Method: " + callerMethod, false);
    }
  }

  public static void setLightSusbsytemDefaultCommandFromMap(
      LightSubsystem subsystem, HashMap<LightSubsystem, Command> map) {
    subsystem.setDefaultCommand(map.get(subsystem));
  }

  public static Distance getLinearMotorSetpointOrZero(SmartMotorController motor) {
    Integration.crashIfIntNotPresent(PresentLibs.Yams);
    Angle motorSetpointAngle = motor.getMechanismPositionSetpoint().orElseGet(Degrees::zero);
    Distance motorSetpoint = motor.getConfig().convertFromMechanism(motorSetpointAngle);
    return motorSetpoint;
  }

  public static boolean isLinearMotorAtSetpoint(SmartMotorController motor, double threshhold) {
    Integration.crashIfIntNotPresent(PresentLibs.Yams);
    Distance motorPosition = motor.getMeasurementPosition();
    return motorPosition.isNear(getLinearMotorSetpointOrZero(motor), threshhold);
  }

  public static void setupBasicMotorLogging(
      String name,
      Table parentTable,
      LogMode loggerheadMode,
      Loggerhead loggerhead,
      SmartMotorController motor,
      EnumSet<DefaultSMCLoggingMode> bmode) {
    Integration.crashIfIntNotPresent(PresentLibs.Yams);

    if (bmode.contains(DefaultSMCLoggingMode.SpeedAngular)) {
      parentTable.addDoubleLogger(
          name + "SpeedRPM", loggerheadMode, () -> motor.getMechanismVelocity().in(RPM));

      parentTable.addDoubleLogger(
          name + "SpeedSetpointRPM",
          loggerheadMode,
          () -> motor.getMechanismSetpointVelocity().orElseGet(RPM::zero).in(RPM));
    }

    if (bmode.contains(DefaultSMCLoggingMode.PositionalLinear)) {
      parentTable.addDoubleLogger(
          name + "DistanceMeters", loggerheadMode, () -> motor.getMeasurementPosition().in(Meters));

      parentTable.addDoubleLogger(
          name + "DistanceSetpointMeters",
          loggerheadMode,
          () -> LoggerheadUtils.getLinearMotorSetpointOrZero(motor).in(Meters));
    }

    if (bmode.contains(DefaultSMCLoggingMode.PositionalAngular)) {
      parentTable.addDoubleLogger(
          name + "AngleDegrees", loggerheadMode, () -> motor.getMechanismPosition().in(Degrees));
      parentTable.addDoubleLogger(
          name + "AngleSetpointDegrees",
          loggerheadMode,
          () -> motor.getMechanismPositionSetpoint().orElseGet(Degrees::zero).in(Degrees));
    }

    parentTable.addDoubleLogger(
        name + "Current", loggerheadMode, () -> motor.getStatorCurrent().in(Amps));
  }

  public static void setupBasicMotorLogging(
      String name,
      Table parentTable,
      LogMode loggerheadMode,
      Loggerhead loggerhead,
      SmartMotorController motor,
      DefaultSMCLoggingMode... bmode) {
    Integration.crashIfIntNotPresent(PresentLibs.Yams);

    EnumSet<DefaultSMCLoggingMode> trueBMode = EnumSet.noneOf(DefaultSMCLoggingMode.class);
    for (DefaultSMCLoggingMode m : bmode) {
      trueBMode.add(m);
    }

    if (trueBMode.isEmpty()) {
      System.out.println(
          "WARNING: Empty bmode on, switching to default behavior and using all basic SMC logging modes "
              + name
              + " \n\n\n\n\n\n\n\n");
    }

    setupBasicMotorLogging(name, parentTable, loggerheadMode, loggerhead, motor, trueBMode);
  }
}
