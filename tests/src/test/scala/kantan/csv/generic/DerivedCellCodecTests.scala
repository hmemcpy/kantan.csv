/*
 * Copyright 2016 Nicolas Rinaudo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package kantan.csv.generic

import imp._
import kantan.codecs.shapeless.laws._
import kantan.csv.generic.arbitrary._
import kantan.csv.laws.discipline.CellCodecTests
import org.scalacheck.{Arbitrary, Gen}
import org.scalatest.FunSuite
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import org.typelevel.discipline.scalatest.Discipline

class DerivedCellCodecTests extends FunSuite with GeneratorDrivenPropertyChecks with Discipline {
  // TODO: let scalacheck-shapeless deal with that when fixed, see
  // https://github.com/alexarchambault/scalacheck-shapeless/issues/50
  implicit def arbOr[A: Arbitrary, B: Arbitrary]: Arbitrary[Or[A, B]] = Arbitrary(
    Gen.oneOf(imp[Arbitrary[Left[A]]].arbitrary, imp[Arbitrary[Right[B]]].arbitrary)
  )

  checkAll("CellCodec[Or[Int, Boolean]]", CellCodecTests[Int Or Boolean].codec[Byte, String])
}
