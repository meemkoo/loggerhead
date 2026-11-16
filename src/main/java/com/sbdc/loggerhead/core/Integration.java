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

import com.sbdc.loggerhead.exceptions.IntegrationException;

public class Integration {
  public static final boolean HAS_COMMANDS =
      isAvailable("edu.wpi.first.wpilibj2.command.CommandScheduler");
  public static final boolean HAS_WPILIB = isAvailable("edu.wpi.first.wpilibj.XboxController");

  public static final boolean HAS_YAMS = isAvailable("yams.motorcontrollers.SmartMotorController");
  public static final boolean HAS_PHOENIX6 =
      isAvailable("com.ctre.phoenix6.swerve.SwerveDrivetrain");

  private static boolean isAvailable(String className) {
    try {
      Class.forName(className, false, Integration.class.getClassLoader());
      return true;
    } catch (ClassNotFoundException | LinkageError e) {
      return false;
    }
  }

  public enum PresentLibs {
    Commands(HAS_COMMANDS),
    WPILib(HAS_WPILIB),
    Yams(HAS_YAMS),
    Phoenix6(HAS_PHOENIX6);

    public final boolean present;

    private PresentLibs(boolean present) {
      this.present = present;
    }
  }

  public static void crashIfIntNotPresent(PresentLibs... ints) {
    for (PresentLibs i : ints) {
      if (!i.present) {
        throw new IntegrationException(i.toString() + " Unavailable. Check your vendordeps!");
      }
    }
  }
}
