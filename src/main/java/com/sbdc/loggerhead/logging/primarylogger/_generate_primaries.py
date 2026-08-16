LICENCE = """

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

"""

from pathlib import Path
import sys, os

FOUND_IT = False
last = Path(__file__).resolve().parent
while not FOUND_IT:
  if 'generators' in os.listdir(last):
    FOUND_IT = True
  else:
    last = last.parent
sys.path.append(str(last))
from generators.run_code_gen import *
print(get_run_mode())

from dataclasses import dataclass, field
import enum
from copy import deepcopy

PUB_CLASS_TEMP = "{fqcls}Publisher"
LOG_ENTRY_CLASS_TEMP = "{fqcls}LogEntry"
FULL_QUALIFIED_TEMP = "Primary{fqcls}Log"

IMPORT_TEMP_ALL = [
"com.sbdc.loggerhead.logging.LogMode",
"edu.wpi.first.networktables.NetworkTableInstance",
"edu.wpi.first.util.datalog.DataLog",
]



@dataclass
class ClassDescriptor:
  base_type: str = ""
  wrapper_type: str = ""
  pub: str = ""
  log: str = ""
  full_class_name: str = ""
  is_array: bool = False


def build_struct_loggers():
  single_class_name = f"PrimaryStructLog"
  array_class_name = f"PrimaryStructArrayLog"


  single_imports = IMPORT_TEMP_ALL.copy()
  single_imports += [
    "edu.wpi.first.networktables.StructPublisher",
    "edu.wpi.first.util.datalog.StructLogEntry",
    "edu.wpi.first.util.struct.Struct"
  ]

  array_imports = IMPORT_TEMP_ALL.copy()
  array_imports += [
    "edu.wpi.first.networktables.StructArrayPublisher",
    "edu.wpi.first.util.datalog.StructArrayLogEntry",
    "edu.wpi.first.util.struct.Struct"
  ]

  single_extension_text = f"<T, S extends Struct<T>> extends AbstractPrimaryLog<T, StructLogEntry<T>, StructPublisher<T>>"
  array_extension_text = f"<T, S extends Struct<T>> extends AbstractPrimaryLog<T[], StructArrayLogEntry<T>, StructArrayPublisher<T>>"

  base_constructor = """super(key, logMode, ntInstance, dataLog);
Struct{arrayMaybe}LogEntry.create(dataLog, key, struct);
setPublisher(ntInstance.getStruct{arrayMaybe}Topic(key, struct).publish());
"""

  base_methods = [
    JavaMethod(name="<ReplaceMe>", 
               is_constructor=True,
               mods=[JavaModifiers.PUBLIC],
               params=[JavaDeclaration("key", "String"),
                       JavaDeclaration("logMode", "LogMode"),
                       JavaDeclaration("ntInstance", "NetworkTableInstance"),
                       JavaDeclaration("dataLog", "DataLog"),
                       JavaDeclaration("struct", "Struct<T>")
                       ],
               body=base_constructor
               ),
    JavaMethod(name="updateFile",
               return_="void",
               is_constructor=False,
               annotations=["Override"],
               mods=[JavaModifiers.PROTECTED],
               params=[JavaDeclaration("newValue", "T{bracketsMaybe}")],
               body="getLogEntry().append(newValue);"
               ),
    JavaMethod(name="updateNetwork",
               return_="void",
               is_constructor=False,
               annotations=["Override"],
               mods=[JavaModifiers.PROTECTED],
               params=[JavaDeclaration("newValue", "T{bracketsMaybe}")],
               body="getPublisher().accept(newValue);"
               )
  ]

  single_methods = deepcopy(base_methods)
  single_methods[0].body = single_methods[0].body.format(arrayMaybe='')
  single_methods[0].name = single_class_name
  for i in single_methods[1:]:
    i.params[0].type = i.params[0].type.format(bracketsMaybe='')

  array_methods = deepcopy(base_methods)
  array_methods[0].body = array_methods[0].body.format(arrayMaybe='Array')
  array_methods[0].name = array_class_name
  for i in array_methods[1:]:
    i.params[0].type = i.params[0].type.format(bracketsMaybe='[]')

  single_file = JavaFile(single_class_name,
                  package='com.sbdc.loggerhead.logging.primarylogger',
                  imports=single_imports,
                  mods=[JavaModifiers.PUBLIC, JavaModifiers.FINAL],
                  class_decl_extension=single_extension_text,
                  fields=[],
                  methods=single_methods)
  array_file = JavaFile(array_class_name,
                  package='com.sbdc.loggerhead.logging.primarylogger',
                  imports=array_imports,
                  mods=[JavaModifiers.PUBLIC, JavaModifiers.FINAL],
                  class_decl_extension=array_extension_text,
                  fields=[],
                  methods=array_methods)
  
  return single_file, array_file


def run_codegen():
  all_cds = [ClassDescriptor() for _ in range(8)]
  for i, wrapper in enumerate(["Boolean", "String", "Integer", "Double"]):
    if wrapper in ("Double", "Boolean"):
      base = wrapper.lower()
    elif wrapper == "Integer":
      base = "long"
    else:
      base = wrapper

    all_cds[i].wrapper_type = wrapper
    all_cds[i].base_type = base
    all_cds[i].pub = PUB_CLASS_TEMP.format(fqcls=wrapper)
    all_cds[i].log = LOG_ENTRY_CLASS_TEMP.format(fqcls=wrapper)
    all_cds[i].full_class_name = wrapper

    array_fq = wrapper + "Array"
    # array_base = base + "[]"

    all_cds[i+4].wrapper_type = wrapper+"[]"
    all_cds[i+4].base_type = base+"[]"
    all_cds[i+4].pub = PUB_CLASS_TEMP.format(fqcls=wrapper+"Array")
    all_cds[i+4].log = LOG_ENTRY_CLASS_TEMP.format(fqcls=wrapper+"Array")
    all_cds[i+4].full_class_name = wrapper+"Array"
    all_cds[i+4].is_array = True


  all_files: list[JavaFile] = []
  for cd in all_cds:
    print("BREAK\t"*15)

    class_name = f"Primary{cd.full_class_name}Log"
    imports = IMPORT_TEMP_ALL.copy()
    imports += [
      "edu.wpi.first.networktables.{cls}".format(cls=cd.pub),
      "edu.wpi.first.util.datalog.{cls}".format(cls=cd.log)
    ]

    if cd.is_array:
      true_type = cd.base_type
    else:
      true_type = cd.wrapper_type

    extension_text = f" extends AbstractPrimaryLog<{true_type}, {cd.log}, {cd.pub}>"

    constructor_body = f"""super(key, logMode, ntInstance, dataLog);
  setLogEntry(new {cd.log}(dataLog, key));
  setPublisher(ntInstance.get{cd.full_class_name}Topic(key).publish());"""

    methods = [
      JavaMethod(name=class_name, 
                 is_constructor=True,
                 mods=[JavaModifiers.PUBLIC],
                 params=[JavaDeclaration("key", "String"),
                         JavaDeclaration("logMode", "LogMode"),
                         JavaDeclaration("ntInstance", "NetworkTableInstance"),
                         JavaDeclaration("dataLog", "DataLog")
                         ],
                 body=constructor_body
                 ),
      JavaMethod(name="updateFile",
                 return_="void",
                 is_constructor=False,
                 annotations=["Override"],
                 mods=[JavaModifiers.PROTECTED],
                 params=[JavaDeclaration("newValue", true_type)],
                 body="getLogEntry().append(newValue);"
                 ),
      JavaMethod(name="updateNetwork",
                 return_="void",
                 is_constructor=False,
                 annotations=["Override"],
                 mods=[JavaModifiers.PROTECTED],
                 params=[JavaDeclaration("newValue", true_type)],
                 body="getPublisher().accept(newValue);"
                 )
    ]

    file = JavaFile(class_name,
                    package='com.sbdc.loggerhead.logging.primarylogger',
                    imports=imports,
                    mods=[JavaModifiers.PUBLIC, JavaModifiers.FINAL],
                    class_decl_extension=extension_text,
                    fields=[],
                    methods=methods,)
    all_files.append(file)
    print(file.generate())

  all_files += build_struct_loggers()

  for i in all_files:
    with open(Path(__file__).parent.joinpath(Path(f"{i.name}.java")), 'w') as f:
      f.write(i.generate())

  pass

run_codegen()
