package model

import org.scalatest.OptionValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import util.Util

class UtilSpec extends AnyFlatSpec with Matchers with OptionValues {

  "Util.makeInt" should "parse a correct integer value" in {
    Util.makeInt("19").value shouldBe 19
  }

  it should "not parse an incorrect integer value" in {
    Util.makeInt("19A").isDefined shouldBe false
  }

  it should "not parse a double value" in {
    Util.makeInt("19.1").isDefined shouldBe false
  }

  "Util.Double" should "parse a correct double value" in {
    Util.makeDouble("19.1").value shouldBe 19.1
  }

  it should "parse a correct integer value" in {
    Util.makeDouble("19").value shouldBe 19.0
  }

  it should "not parse an incorrect double value" in {
    Util.makeInt("19.1A").isDefined shouldBe false
  }
}
