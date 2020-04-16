package model

import cats.syntax.option._
import org.scalatest.OptionValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class DistanceSpec extends AnyFlatSpec with Matchers with OptionValues{

  "Distance" should "parse the correct km > 1 distance" in {
    Distance.parse("Up to 1.75km".some).value shouldBe 1750
  }

  it should "parse the correct km < 1 distance" in {
    Distance.parse("Up to 0.75km".some).value shouldBe 750
  }

  it should "parse the correct distance trimming the text" in {
    Distance.parse("   Up to 0.75km ".some).value shouldBe 750
  }

  it should "parse the correct km = 1 distance" in {
    Distance.parse("Up to 1km".some).value shouldBe 1000
  }

  it should "parse the .3km distance" in {
    Distance.parse("Up to .3km".some).value shouldBe 300
  }

  it should "parse the correct m distance" in {
    Distance.parse("Up to 850m".some).value shouldBe 850
  }
}
