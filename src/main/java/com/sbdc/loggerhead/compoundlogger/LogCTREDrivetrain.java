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

package com.sbdc.loggerhead.compoundlogger;

import com.ctre.phoenix6.swerve.SwerveDrivetrain;
import com.sbdc.loggerhead.LogMode;
import com.sbdc.loggerhead.Loggerhead;
import com.sbdc.loggerhead.OneShot;
import com.sbdc.loggerhead.Table;

public class LogCTREDrivetrain implements CompoundLogger {
  private final SwerveDrivetrain<?, ?, ?> drivetrain;
  private final LogMode logMode;
  private final String name;

  public LogCTREDrivetrain(String name, LogMode logMode, SwerveDrivetrain<?, ?, ?> drivetrain) {
    this.name = name;
    this.drivetrain = drivetrain;
    this.logMode = logMode;
  }

  @Override
  public void initialize(Table parentTable) {
    String logRootSwerve = parentTable.path + name + "/Swerve/";
    String logRootPose = parentTable.path + name + "/Pose/";

    // OneShot.setString(logRootSwerve + ".type", "SwerveDrive");
    OneShot.setString(logRootPose + ".type", "Field2d");

    Loggerhead.getInstance()
        .addPoseLogger(logRootPose + "Robot", logMode, () -> drivetrain.getState().Pose)
        .addSwerveStateLogger(logRootSwerve, logMode, () -> drivetrain.getState().ModuleStates);
  }

  @Override
  public LogMode getLogMode() {
    return logMode;
  }
}
