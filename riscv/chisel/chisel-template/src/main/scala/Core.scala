package fetch

import chisel3._
import chisel3.util._
import common.Consts._


class Core extends Module {
    val io = IO(new Bundle {
        val imem = Flipped(new ImemPortIo())
        val exit = Output(Bool())
    })

    val regfile = Mem(32, UInt(WORD_LEN.W))

    //********************************************
    // 命令フェッチ (Instruction Fetch) ステージ 
    //********************************************

    // 初期値 START_ADDR でプログラムカウンタを生成する
    val pc_reg = RegInit(START_ADDR)
    // 1サイクルで4Byteずつカウントアップ
    pc_reg := pc_reg + 4.U(WORD_LEN.W)

    // 出力ポート addr に pc_reg を接続
    io.imem.addr := pc_reg
    // 入力ポート inst の値を、inst で受ける
    val inst = io.imem.inst

    // デバッグ用の出力
    printf(p"pc_reg: 0x${Hexadecimal(pc_reg)}\n")
    printf(p"inst: 0x${Hexadecimal(inst)}\n")
    printf("----------\n")

    // inst 信号が 0x34333231 になったらプログラムを終了
    io.exit := (inst === 0x34333231.U(WORD_LEN.W))
}