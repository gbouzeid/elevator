package com.elevis.platform.common

/**
 * A simple command enum - for the Main/Interfaces
 * User: bouzeig
 */
class Command extends Enumeration {
    type status = Value
    val Status = Value("status")
    val Help = Value("help")
    val Add = Value("add")
    val Exit = Value("exit")
  }

object Command extends Command