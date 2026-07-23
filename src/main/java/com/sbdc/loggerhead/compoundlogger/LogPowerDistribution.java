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

import com.sbdc.loggerhead.LogMode;
import com.sbdc.loggerhead.Table;

import edu.wpi.first.wpilibj.PowerDistribution;

public class LogPowerDistribution implements CompoundLogger {

    private final LogMode logMode;
    private final PowerDistribution pd;

    public LogPowerDistribution(LogMode logMode, PowerDistribution pd) {
        this.logMode = logMode;
        this.pd = pd;
    }

    @Override
    public LogMode getLogMode() {
        return logMode;
    }

    
    @Override
    public void initialize(Table parentTable) {
        parentTable
            .addBooleanLogger("switchableChannel", logMode, () -> pd.getSwitchableChannel())
            .addDoubleLogger("voltage", logMode, () -> pd.getVoltage())
            .addDoubleLogger("totalCurrentAmps", logMode, () -> pd.getTotalCurrent())
            .addDoubleLogger("tempratureCelcuius", logMode, () -> pd.getTemperature())
        ;

        for (int i = 0; i < pd.getNumChannels(); i++) {
            final int inneri = i;
            parentTable.addDoubleLogger("channel_"+i, logMode, () -> pd.getCurrent(inneri));
        }
    }
}
