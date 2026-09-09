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

package com.sbdc.loggerhead.util;

import edu.wpi.first.wpilibj2.command.CommandScheduler;
import edu.wpi.first.wpilibj2.command.Subsystem;
import java.util.ArrayList;
import java.util.List;

/** Implmentation of a subsystem similar to SubsystemBase without registering to LiveWindow */
public class LightSubsystem implements Subsystem {
  private static List<Subsystem> lightSusbsystemList = new ArrayList<>();

  private static void registerLightSubsystem(Subsystem subsystem) {
    lightSusbsystemList.add(subsystem);
  }

  public static List<Subsystem> getAllLightSubsystems() {
    return lightSusbsystemList;
  }

  public LightSubsystem() {
    CommandScheduler.getInstance().registerSubsystem(this);
    LightSubsystem.registerLightSubsystem(this);
  }
}
