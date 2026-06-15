package rv32cpu

import chisel3._
import chisel3.util._
import common.Consts._
import common.RV32I._


class Core extends Module {
    val io = IO(new Bundle {
        val imem = Flipped(new ImemPortIo())
        val dmem = Flipped(new DmemPortIo())
        val exit = Output(Bool())
    })

    val regfile = Mem(32, UInt(WORD_LEN.W))

    //********************************************
    // 命令フェッチ (IF) ステージ 
    //********************************************

    // 初期値 START_ADDR でプログラムカウンタを生成する
    val pc_reg = RegInit(START_ADDR)
    // 1サイクルで4Byteずつカウントアップ
    pc_reg := pc_reg + 4.U(WORD_LEN.W)

    // 出力ポート addr に pc_reg を接続
    io.imem.addr := pc_reg
    // 入力ポート inst の値を、inst で受ける
    val inst = io.imem.inst

    //********************************************
    // 命令デコード (ID) ステージ 
    //********************************************

    // レジスタアドレスの取得
    val rs1_addr = inst(19, 15)
    val rs2_addr = inst(24, 20)
    val wb_addr  = inst(11, 7)

    // 常に0番レジスタでゼロを保持するためにマルチプレクサを通してデータを取得
    val rs1_data = Mux((rs1_addr =/= 0.U(WORD_LEN.W)), regfile(rs1_addr), 0.U(WORD_LEN.W))
    val rs2_data = Mux((rs2_addr =/= 0.U(WORD_LEN.W)), regfile(rs2_addr), 0.U(WORD_LEN.W))

    // 即値を符号拡張で32ビットに変換
    val imem_i = inst(31, 20)
    val imem_i_sext = Cat(Fill(20, imem_i(11)), imem_i)

    //********************************************
    // 実行 (EX) ステージ 
    //********************************************
    
    // MuxCase で ALU を実装
    val alu_out = MuxCase(0.U(WORD_LEN.W), Seq(
        (inst === LW) -> (rs1_data + imem_i_sext)   // LW 命令: メモリアドレスの計算
    ))

    //********************************************
    // メモリアクセス (MEM) ステージ 
    //********************************************

    // io.dmem を通してメモリにアドレスを渡す
    io.dmem.addr := alu_out

    //********************************************
    // ライトバック (WB) ステージ 
    //********************************************

    val wb_data = io.dmem.rdata
    when(inst === LW) {
        regfile(wb_addr) := wb_data
    }

    // デバッグ用の出力
    printf(p"pc_reg    : 0x${Hexadecimal(pc_reg)}\n")
    printf(p"inst      : 0x${Hexadecimal(inst)}\n")
    printf(p"rs1_addr  : $rs1_addr\n")
    printf(p"rs2_addr  : $rs2_addr\n")
    printf(p"wb_addr   : $wb_addr\n")
    printf(p"rs1_data  : 0x${Hexadecimal(rs1_data)}\n")
    printf(p"rs2_data  : 0x${Hexadecimal(rs2_data)}\n")
    printf(p"wb_data   : 0x${Hexadecimal(wb_data)}\n") // 追加
    printf(p"dmem.addr : ${io.dmem.addr}\n")
    printf("----------\n")

    // プログラムを終了判定
    io.exit := (inst === 0x14131211.U(WORD_LEN.W))
}