// 各桁の生成・伝播信号の判定
module gp_generator(
    input  logic a, b,
    output logic g, p
);
    always_comb begin
        g = a & b;
        p = a | b;
    end
endmodule

// プリフィックスセルの実装
module prefix_cell(
    input  logic gin1, pin1, gin2, pin2,
    output logic gout, pout
);
    // gin1: g_{i:k}, pin1: p_{i:k}, gin2: g_{k-1:j}, pin2: p_{k-1:j}
    // gout: g_{i:j}, pout: p_{i:j} 
    always_comb begin
        gout = gin1 | (pin1 & gin2);
        pout = pin1 & pin2;
    end
endmodule

// プリフィックス加算器本体
module prefix_adder
#(parameter NBIT = 32)
(
    input  logic [(NBIT - 1):0] a, b,
    output logic [(NBIT - 1):0] s,
    output logic                cout
);
    logic [(NBIT - 1):0] g, p;
    logic [(NBIT - 1):0] carry;
    genvar i, j;
    parameter stage_num = $clog2(NBIT);

    // 生成信号と伝播信号の計算
    generate
        for (i = 0; i < NBIT; i++) begin : genGP
            gp_generator(.a(a[i]), .b(b[i]), .g(g[i]), .p(p[i]))
        end
    endgenerate

    // プレフィックスセルの計算
    logic [(NBIT - 1):0] g_stage [(stage_num - 1):0];
    logic [(NBIT - 1):0] p_stage [(stage_num - 1):0];
    generate
        for (i = 0; i < stage_num; i++) begin : genPrefixCells
            for (j = 0; j < N; j++) begin : genPrefixCellStage
                
            end
        end
    endgenerate

    // 各桁の出力を計算

endmodule