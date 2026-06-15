package rv32cpu

import chisel3._
import chisel3.util._


class Top extends Module {
    val io = IO(new Bundle {
        val exit = Output(Bool())
    })

    // Core クラスと Memory クラスを new でインスタンス化し、Module でハードウェア化
    val core = Module(new Core())
    val memory = Module(new Memory())

    // core と memory の io を一括接続
    core.io.imem <> memory.io.imem
    core.io.dmem <> memory.io.dmem

    io.exit := core.io.exit
}