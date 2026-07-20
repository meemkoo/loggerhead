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

package com.sbdc.loggerhead.primarylogger;

import com.sbdc.loggerhead.LogMode;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.StructArrayPublisher;
import edu.wpi.first.util.datalog.DataLog;
import edu.wpi.first.util.datalog.StructArrayLogEntry;

/**
 * Implementation of {@link AbstractPrimaryLog} for an array of {@link SwerveModuleState} structs.
 * Note that most tooling assumes the length of any swerve state array to have a length of 4.
 */
public final class PrimarySwerveStateLog
    extends AbstractPrimaryLog<
        SwerveModuleState[],
        StructArrayLogEntry<SwerveModuleState>,
        StructArrayPublisher<SwerveModuleState>> {

  /**
   * Create a new swerve state logger. See {@link SwerveModuleState}, {@link StructArrayLogEntry},
   * {@link StructArrayPublisher}
   *
   * @param key Fully qualified path of this swerve state logger
   * @param logMode LogMode this logger is configured in
   * @param ntInstance Reference to NT instance
   * @param dataLog Reference to DataLog instance
   */
  public PrimarySwerveStateLog(
      String key, LogMode logMode, NetworkTableInstance ntInstance, DataLog dataLog) {
    super(key, logMode, ntInstance, dataLog);
    setLogEntry(StructArrayLogEntry.create(dataLog, key, SwerveModuleState.struct));
    setPublisher(ntInstance.getStructArrayTopic(key, SwerveModuleState.struct).publish());
  }

  @Override
  protected void updateFile(SwerveModuleState[] newValue) {
    getLogEntry().append(newValue);
  }

  @Override
  protected void updateNetwork(SwerveModuleState[] newValue) {
    getPublisher().accept(newValue);
  }
}
