`include "halfadder.sv"

module test_halfadder();
    logic a, b; // 入力信号
    logic s, c; // 出力信号

    // 半加算器のインスタンスを作成
    halfadder _halfadder (
        .a(a),
        .b(b),
        .s(s),
        .c(c)
    );

    initial begin
        $display("a b || s c");
        $display("----++----");

        // テストパターンを試す
        a = 0; b = 0; #10; $display("%d %d || %d %d", a, b, s, c);
        a = 0; b = 1; #10; $display("%d %d || %d %d", a, b, s, c);
        a = 1; b = 0; #10; $display("%d %d || %d %d", a, b, s, c);
        a = 1; b = 1; #10; $display("%d %d || %d %d", a, b, s, c);
    end
endmodule