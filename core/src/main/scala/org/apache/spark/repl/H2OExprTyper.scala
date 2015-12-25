/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

/**
 * This code is based on code org.apache.spark.repl.SparkExprTyper released under Apache 2.0"
 * Link on Github: https://github.com/apache/spark/blob/master/repl/scala-2.10/src/main/scala/org/apache/spark/repl/SparkExprTyper.scala
 * Author: Paul Phillips
 */
package org.apache.spark.repl

import org.apache.spark.Logging

import scala.tools.nsc.ast.parser.Tokens.EOF
import scala.tools.nsc.interpreter._

private[repl] trait H2OExprTyper extends Logging {
  val repl: H2OIMain

  import repl._
  import global.{Import => _, reporter => _, _}
  import definitions._
  import naming.freshInternalVarName
  import syntaxAnalyzer.UnitParser

  object codeParser extends { val global: repl.global.type = repl.global } with CodeHandlers[Tree] {
    def applyRule[T](code: String, rule: UnitParser => T): T = {
      reporter.reset()
      val scanner = newUnitParser(code)
      val result  = rule(scanner)

      if (!reporter.hasErrors) {
        scanner.accept(EOF)
      }

      result
    }

    def defns(code: String) = stmts(code) collect { case x: DefTree => x }
    def expr(code: String)  = applyRule(code, _.expr())
    def stmts(code: String) = applyRule(code, _.templateStats())
    def stmt(code: String)  = stmts(code).last  // guaranteed nonempty
  }

  /** Parse a line into a sequence of trees. Returns None if the input is incomplete. */
  def parse(line: String): Option[List[Tree]] = debugging(s"""parse("$line")""")  {
    var isIncomplete = false
    reporter.withIncompleteHandler((_, _) => isIncomplete = true) {
      val trees = codeParser.stmts(line)
      if (reporter.hasErrors) {
        Some(Nil)
      } else if (isIncomplete) {
        None
      } else {
        Some(trees)
      }
    }
  }
  // def parsesAsExpr(line: String) = {
  //   import codeParser._
  //   (opt expr line).isDefined
  // }

  def symbolOfLine(code: String): Symbol = {
    def asExpr(): Symbol = {
      val name  = freshInternalVarName()
      // Typing it with a lazy val would give us the right type, but runs
      // into compiler bugs with things like existentials, so we compile it
      // behind a def and strip the NullaryMethodType which wraps the expr.
      val line = "def " + name + " = {\n" + code + "\n}"

      interpretSynthetic(line) match {
        case IR.Success =>
          val sym0 = symbolOfTerm(name)
          // drop NullaryMethodType
          val sym = sym0.cloneSymbol setInfo afterTyper(sym0.info.finalResultType)
          if (sym.info.typeSymbol eq UnitClass) NoSymbol else sym
        case _          => NoSymbol
      }
    }
    def asDefn(): Symbol = {
      val old = repl.definedSymbolList.toSet

      interpretSynthetic(code) match {
        case IR.Success =>
          repl.definedSymbolList filterNot old match {
            case Nil        => NoSymbol
            case sym :: Nil => sym
            case syms       => NoSymbol.newOverloaded(NoPrefix, syms)
          }
        case _ => NoSymbol
      }
    }
    beQuietDuring(asExpr()) orElse beQuietDuring(asDefn())
  }

  private var typeOfExpressionDepth = 0
  def typeOfExpression(expr: String, silent: Boolean = true): Type = {
    if (typeOfExpressionDepth > 2) {
      logDebug("Terminating typeOfExpression recursion for expression: " + expr)
      return NoType
    }
    typeOfExpressionDepth += 1
    // Don't presently have a good way to suppress undesirable success output
    // while letting errors through, so it is first trying it silently: if there
    // is an error, and errors are desired, then it re-evaluates non-silently
    // to induce the error message.
    try beSilentDuring(symbolOfLine(expr).tpe) match {
      case NoType if !silent => symbolOfLine(expr).tpe // generate error
      case tpe               => tpe
    }
    finally typeOfExpressionDepth -= 1
  }
}
