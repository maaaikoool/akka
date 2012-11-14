/**
 * Copyright (C) 2009-2012 Typesafe Inc. <http://www.typesafe.com>
 */
package akka.util

import org.scalatest.WordSpec
import org.scalatest.matchers.MustMatchers
import duration._
import akka.testkit.AkkaSpec
import akka.testkit.TestLatch
import java.util.concurrent.TimeoutException
import akka.dispatch.Await
import akka.testkit.TimingTest

class DurationSpec extends AkkaSpec {

  "A HashedWheelTimer" must {

    "not mess up long timeouts" taggedAs TimingTest in {
      val longish = Timeout(Long.MaxValue)
      val barrier = TestLatch()
      val job = system.scheduler.scheduleOnce(longish.duration)(barrier.countDown())
      intercept[TimeoutException] {
        // this used to fire after 46 seconds due to wrap-around
        Await.ready(barrier, 90 seconds)
      }
      job.cancel()
    }

  }

  "Duration" must {

    "form a one-dimensional vector field" in {
      val zero = 0.seconds
      val one = 1.second
      val two = one + one
      val three = 3 * one
      (0 * one) must be(zero)
      (2 * one) must be(two)
      (three - two) must be(one)
      (three / 3) must be(one)
      (two / one) must be(2)
      (one + zero) must be(one)
      (one / 1000000) must be(1.micro)
    }

    "respect correct treatment of infinities" in {
      val one = 1.second
      val inf = Duration.Inf
      val minf = Duration.MinusInf
      (-inf) must be(minf)
      intercept[IllegalArgumentException] { minf + inf }
      intercept[IllegalArgumentException] { inf - inf }
      intercept[IllegalArgumentException] { inf + minf }
      intercept[IllegalArgumentException] { minf - minf }
      (inf + inf) must be(inf)
      (inf - minf) must be(inf)
      (minf - inf) must be(minf)
      (minf + minf) must be(minf)
      assert(inf == inf)
      assert(minf == minf)
      inf.compareTo(inf) must be(0)
      inf.compareTo(one) must be(1)
      minf.compareTo(minf) must be(0)
      minf.compareTo(one) must be(-1)
      assert(inf != minf)
      assert(minf != inf)
      assert(one != inf)
      assert(minf != one)
    }

    "support fromNow" in {
      val dead = 2.seconds.fromNow
      val dead2 = 2 seconds fromNow
      // view bounds vs. very local type inference vs. operator precedence: sigh
      dead.timeLeft must be > (1 second: Duration)
      dead2.timeLeft must be > (1 second: Duration)
      1.second.sleep
      dead.timeLeft must be < (1 second: Duration)
      dead2.timeLeft must be < (1 second: Duration)
    }

  }

}
