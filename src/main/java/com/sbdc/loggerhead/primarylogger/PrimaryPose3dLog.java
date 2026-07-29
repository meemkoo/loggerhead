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
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.StructPublisher;
import edu.wpi.first.util.datalog.DataLog;
import edu.wpi.first.util.datalog.StructLogEntry;

public final class PrimaryPose3dLog
    extends AbstractPrimaryLog<Pose3d, StructLogEntry<Pose3d>, StructPublisher<Pose3d>> {
  public PrimaryPose3dLog(
      String key, LogMode logMode, NetworkTableInstance ntInstance, DataLog dataLog) {
    super(key, logMode, ntInstance, dataLog);
    setLogEntry(StructLogEntry.create(dataLog, key, Pose3d.struct));
    setPublisher(ntInstance.getStructTopic(key, Pose3d.struct).publish());
  }

  @Override
  protected void updateFile(Pose3d newValue) {
    getLogEntry().append(newValue);
  }

  @Override
  protected void updateNetwork(Pose3d newValue) {
    getPublisher().accept(newValue);
  }
}
