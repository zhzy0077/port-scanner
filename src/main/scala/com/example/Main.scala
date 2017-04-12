package com.example

import javafx.application.Application

object Main {
  def main(args: Array[String]): Unit = {
    Application.launch(classOf[App])
  }
  implicit class RubyKestrel[A](val repr: A) extends AnyVal {
    def tap[B](f: A => B): A = { f(repr); repr }
  }
}
