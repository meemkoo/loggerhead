package com.sbdc.loggerhead.primarylogger;

import com.sbdc.loggerhead.LogMode;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.StructArrayPublisher;
import edu.wpi.first.util.datalog.DataLog;
import edu.wpi.first.util.datalog.StructArrayLogEntry;
import edu.wpi.first.util.struct.Struct;

public final class PrimaryStructArrayLog<T, S extends Struct<T>>
    extends AbstractPrimaryLog<T[], StructArrayLogEntry<T>, StructArrayPublisher<T>> {
  public PrimaryStructArrayLog(
      String key,
      LogMode logMode,
      NetworkTableInstance ntInstance,
      DataLog dataLog,
      Struct<T> struct) {
    super(key, logMode, ntInstance, dataLog);

    setLogEntry(StructArrayLogEntry.create(dataLog, key, struct));
    setPublisher(ntInstance.getStructArrayTopic(key, struct).publish());
  }

  @Override
  protected void updateFile(T[] newValue) {
    getLogEntry().append(newValue);
  }

  @Override
  protected void updateNetwork(T[] newValue) {
    getPublisher().accept(newValue);
  }
}
