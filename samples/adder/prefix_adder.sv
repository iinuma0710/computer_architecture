// 各桁の生成・伝播信号の判定
module gp_generator(
    input  logic a, b,
    output logic g, p
);
    always_comb begin
        g = a & b;
        p = a ^ b;
    end
endmodule

// プリフィックスセルの実装
module prefix_cell(
    input  logic gin1, pin1, gin2, pin2,
    output logic gout, pout
); 
    always_comb begin
        gout = gin2 | (pin2 & gin1);
        pout = pin2 & pin1;
    end
endmodule

// プリフィックス加算器本体
module prefix_adder
#(parameter NBIT = 32)
(
    input  logic [(NBIT - 1):0] a, b,
    input  logic                cin,
    output logic [(NBIT - 1):0] s,
    output logic                cout
);
    genvar i, j;
    parameter STAGE_NUM = $clog2(NBIT) + 1;
    logic [(NBIT - 1):0] g [STAGE_NUM:0];
    logic [(NBIT - 1):0] p [STAGE_NUM:0];
    logic [(NBIT - 1):0] carry;

    generate
        // 生成信号と伝播信号の計算
        for (i = 0; i < NBIT; i++) begin : genGP
            gp_generator(.a(a[i]), .b(b[i]), .g(g[0][i]), .p(p[0][i]))
        end

        // 最終段を除くプリフィックスツリーの計算
        for (i = 1; i < STAGE_NUM; i++) begin : genPrefixTree
            for (j = 0; j < NBIT; j++) begin : genPrefixStage
                if ((j >= (1 << (i - 1))) & (i % 2 == 1)) begin
                    prefix_cell(
                        .gin1(g[i - 1][j - (1 << (i - 1))]), .pin1(p[i - 1][j - (1 << (i - 1))]),
                        .gin2(g[i - 1][j]), .pin2(p[i - 1][j]),
                        .gout1(g[i][j]), .pout1(p[i][j]) 
                    );
                end else begin
                    assign g[i][j] = g[i - 1][j];
                    assign p[i][j] = p[i - 1][j];
                end
            end
        end

        for (j = 0; j < NBIT; j++) begin : genCarry
            // 最終段のプリフィックスセルの計算
            if ((j >= 2) & (j % 2 == 0)) begin
                prefix_cell(
                    .gin1(g[STAGE_NUM - 1][j - 1]), .pin1(p[STAGE_NUM - 1][j - 1]),
                    .gin2(g[STAGE_NUM - 1][j]), .pin2(p[STAGE_NUM - 1][j]),
                    .gout1(g[STAGE_NUM][j]), .pout1(p[STAGE_NUM][j]) 
                );
            end else begin
                assign g[STAGE_NUM][j] = g[STAGE_NUM - 1][j];
                assign p[STAGE_NUM][j] = p[STAGE_NUM - 1][j];
            end

            // 桁上げと和の計算
            if (j == 0) begin
                assign carry[j] = g[STAGE_NUM][j] | (p[STAGE_NUM][j] & cin);
                assign s[j] = p[0][j] ^ cin;
            end else begin
                assign carry[j] = g[STAGE_NUM][j] | (p[STAGE_NUM][j] & carry[j - 1]);
                assign s[j] = p[0][j] ^ carry[j - 1];
            end
        end

        // 桁上げ出力 cout の代入
        assign cout = carry[NBIT - 1];
    endgenerate
endmodule