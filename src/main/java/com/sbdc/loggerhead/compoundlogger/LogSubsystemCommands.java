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
import com.sbdc.loggerhead.Loggerhead;
import com.sbdc.loggerhead.OneShot;
import com.sbdc.loggerhead.Table;

import edu.wpi.first.wpilibj2.command.Subsystem;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

/**
 * CompoundLogger for subsystems tracking things about commands on a subsystem. Based off of {@link
 * SubsystemBase#initSendable}. Logs: The subsystem's default command name, wether or not the
 * subsystem has a default command, wether or not a command is running, and the current command.ks
 */
public class LogSubsystemCommands implements CompoundLogger {
  private final LogMode logMode;
  private final String name;

  private final Subsystem subsystem;

  /**
   * @param name The path that the subsystem's commands will get logged under. Does not end or begin
   *     with a slash.
   * @param logMode Logging mode
   * @param subsystem Subsystem to be logged
   */
  public LogSubsystemCommands(String name, LogMode logMode, Subsystem subsystem) {
    this.subsystem = subsystem;
    this.logMode = logMode;
    this.name = name;
  }

  @Override
  public void initialize(Table parentTable) {
    String logRoot = parentTable.path + name + "/";

    // Publish only to network tables that it is of type subsystem (for display in dashboards)
    OneShot.setString(logRoot + ".type", "Subsystem");

    Loggerhead.getInstance()
        .addBooleanLogger(
            logRoot + ".hasDefault", logMode, () -> subsystem.getDefaultCommand() != null)
        .addStringLogger(
            logRoot + ".default",
            logMode,
            () ->
                subsystem.getDefaultCommand() != null
                    ? subsystem.getDefaultCommand().getName()
                    : "none")
        .addBooleanLogger(
            logRoot + ".hasCommand", logMode, () -> subsystem.getCurrentCommand() != null)
        .addStringLogger(
            logRoot + ".command",
            logMode,
            () ->
                subsystem.getCurrentCommand() != null
                    ? subsystem.getCurrentCommand().getName()
                    : "none");
  }

  @Override
  public void update() {}

  @Override
  public LogMode getLogMode() {
    return this.logMode;
  }
}
