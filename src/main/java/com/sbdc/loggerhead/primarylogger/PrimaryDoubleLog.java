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
import edu.wpi.first.networktables.DoublePublisher;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.util.datalog.DataLog;
import edu.wpi.first.util.datalog.DoubleLogEntry;

public final class PrimaryDoubleLog
    extends AbstractPrimaryLog<Double, DoubleLogEntry, DoublePublisher> {
  public PrimaryDoubleLog(
      String key, LogMode logMode, NetworkTableInstance ntInstance, DataLog dataLog) {
    super(key, logMode, ntInstance, dataLog);
    setLogEntry(new DoubleLogEntry(dataLog, key));
    setPublisher(ntInstance.getDoubleTopic(key).publish());
  }

  @Override
  protected void updateFile(Double newValue) {
    getLogEntry().append(newValue);
  }

  @Override
  protected void updateNetwork(Double newValue) {
    getPublisher().accept(newValue);
  }
}
