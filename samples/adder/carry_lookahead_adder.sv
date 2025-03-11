`include "fulladder.sv"

module adder_block
    #(parameter NBIT = 4)
    (
        input  logic [(NBIT - 1):0] a, b,
        input  logic cin,
        output logic [(NBIT - 1):0] s,
        output logic cout
    );
    // 各全加算機の出力信号
    logic [(NBIT - 1):0] fa_cout;

    // 生成信号と伝播信号
    logic [(NBIT - 1):0] g, p;
    logic [(NBIT - 1):0] block_g, block_p;

    genvar i;
    generate
        // NBIT 個の全加算器と生成・伝播信号の演算
        for (i = 0; i < NBIT; i++) begin : genAdderBlock
            fulladder fa(
                .a(a[i]), .b(b[i]), .cin(i == 0 ? cin : fa_cout[i - 1]), 
                .s(s[i]), .cout(fa_cout[i])
            );
            assign g[i] = a[i] & b[i];
            assign p[i] = a[i] | b[i];
            assign block_g[i] = i == 0 ? g[0] :  g[i] | (p[i] & block_g[i - 1]);
            assign block_p[i] = i == 0 ? p[0] :  p[i] & block_p[i - 1];
        end
        assign cout = block_g[NBIT - 1] | (block_p[NBIT - 1] & cin);
    endgenerate
endmodule

module carry_lookahead_adder
    #(parameter NBIT = 32, NBIT_BLOCK = 4)
    (
        input  logic[(NBIT - 1):0] a, b,
        output logic[(NBIT - 1):0] s,
        output logic cout
    );
    // 加算器ブロックの数
    localparam int BLOCK_NUM = NBIT / NBIT_BLOCK;

    // ブロックごとの桁上げ出力
    logic [(BLOCK_NUM - 1):0] block_cout;

    genvar i;
    generate
        // BLOCK_NUM 個の加算器ブロックを接続
        for (i = 0; i < BLOCK_NUM; i++) begin : genCarryLookaheadAdder
            localparam int start_idx = NBIT_BLOCK * i;
            localparam int end_idx = NBIT_BLOCK * (i + 1) - 1;
            adder_block ab(
                .a(a[end_idx:start_idx]), .b(b[end_idx:start_idx]), .cin(i==0?1'b0:block_cout[i - 1]),
                .s(s[end_idx:start_idx]), .cout(block_cout[i])
            ); 
        end
        assign cout = block_cout[BLOCK_NUM - 1];
    endgenerate
endmodule