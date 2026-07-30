package com.sbdc.loggerhead.primarylogger;

import com.sbdc.loggerhead.LogMode;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.StructPublisher;
import edu.wpi.first.util.datalog.DataLog;
import edu.wpi.first.util.datalog.StructLogEntry;
import edu.wpi.first.util.struct.Struct;

public final class PrimaryStructLog<T, S extends Struct<T>>
    extends AbstractPrimaryLog<T, StructLogEntry<T>, StructPublisher<T>> {
  public PrimaryStructLog(
      String key,
      LogMode logMode,
      NetworkTableInstance ntInstance,
      DataLog dataLog,
      Struct<T> struct) {
    super(key, logMode, ntInstance, dataLog);

    setLogEntry(StructLogEntry.create(dataLog, key, struct));
    setPublisher(ntInstance.getStructTopic(key, struct).publish());
  }

  @Override
  protected void updateFile(T newValue) {
    getLogEntry().append(newValue);
  }

  @Override
  protected void updateNetwork(T newValue) {
    getPublisher().accept(newValue);
  }
}
