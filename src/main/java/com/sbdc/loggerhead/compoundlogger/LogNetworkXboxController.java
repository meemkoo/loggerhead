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
import edu.wpi.first.networktables.BooleanPublisher;
import edu.wpi.first.networktables.DoublePublisher;
import edu.wpi.first.networktables.IntegerPublisher;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;

/** A {@link CompoundLogger} which logs all inputs from a {@link CommandXboxController} */
public class LogNetworkXboxController implements CompoundLogger {
  private final XboxController hid;
  private final String name;

  NetworkTableInstance inst = NetworkTableInstance.getDefault();

  NetworkTable controllerRoot;

  NetworkTable axes;
  NetworkTable buttons;
  NetworkTable povs;

  BooleanPublisher a;
  BooleanPublisher b;
  BooleanPublisher x;
  BooleanPublisher y;

  BooleanPublisher lb;
  BooleanPublisher rb;

  BooleanPublisher ls;
  BooleanPublisher rs;

  BooleanPublisher back;
  BooleanPublisher start;

  IntegerPublisher pov;

  DoublePublisher lx;
  DoublePublisher rx;
  DoublePublisher ly;
  DoublePublisher ry;

  DoublePublisher lt;
  DoublePublisher rt;

  /**
   * @param name base name of the controller. Note that the telemetry/logging name will be prefixed
   *     with {@code Controller} and postfixed with which DS slot it occupies.
   * @param controller the {@link CommandXboxController} to log
   */
  public LogNetworkXboxController(String name, CommandXboxController controller) {
    this.hid = controller.getHID();
    this.name = name;
  }

  @Override
  public void initialize(String parentTable) {
    // TODO Make declaritive, dont undercut Loggerhead
    controllerRoot = inst.getTable(parentTable + this.name);

    axes = controllerRoot.getSubTable("Axes");
    buttons = controllerRoot.getSubTable("Buttons");
    povs = controllerRoot.getSubTable("POVs");

    a = buttons.getBooleanTopic("A").publish();
    b = buttons.getBooleanTopic("B").publish();
    x = buttons.getBooleanTopic("X").publish();
    y = buttons.getBooleanTopic("Y").publish();

    lb = buttons.getBooleanTopic("LeftBumper").publish();
    rb = buttons.getBooleanTopic("RightBumper").publish();

    back = buttons.getBooleanTopic("Back").publish();
    start = buttons.getBooleanTopic("Start").publish();

    ls = buttons.getBooleanTopic("LeftStick").publish();
    rs = buttons.getBooleanTopic("RightStick").publish();

    pov = povs.getIntegerTopic("DPad").publish();

    lx = axes.getDoubleTopic("LeftX").publish();
    rx = axes.getDoubleTopic("RightX").publish();
    ly = axes.getDoubleTopic("LeftY").publish();
    ry = axes.getDoubleTopic("RightY").publish();

    lt = axes.getDoubleTopic("LeftTrigger").publish();
    rt = axes.getDoubleTopic("RightTrigger").publish();
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void update() {
    a.set(hid.getAButton());
    b.set(hid.getBButton());
    x.set(hid.getXButton());
    y.set(hid.getYButton());
    lb.set(hid.getLeftBumperButton());
    rb.set(hid.getRightBumperButton());
    back.set(hid.getBackButton());
    start.set(hid.getStartButton());
    ls.set(hid.getLeftStickButton());
    rs.set(hid.getRightStickButton());

    pov.set(hid.getPOV());

    lx.set(hid.getLeftX());
    rx.set(hid.getRightX());
    ly.set(hid.getLeftY());
    ry.set(hid.getRightY());

    lt.set(hid.getLeftTriggerAxis());
    rt.set(hid.getRightTriggerAxis());
  }

  @Override
  public LogMode getLogMode() {
    return LogMode.NetworkOnly;
  }
}
