package rv32cpu

import chisel3._
import chisel3.simulator.EphemeralSimulator._
import org.scalatest.flatspec.AnyFlatSpec

import common.Consts._

class HexTest extends AnyFlatSpec {
    "mycpu" should "work through hex" in {
        simulate(new Top) { c =>
            // 明示的にリセットをかける
            c.reset.poke(true.B)
            c.clock.step(1)
            c.reset.poke(false.B)
            c.clock.step(1)
            
            // テスト内容を記述
            while (!c.io.exit.peek().litToBoolean) {
                c.clock.step(1)
            }
        }
    }
}