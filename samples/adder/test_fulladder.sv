`include "fulladder.sv"

module test_halfadder();
    logic a, b, cin; // 入力信号
    logic s, cout;   // 出力信号

    // 半加算器のインスタンスを作成
    fulladder _fulladder (.a(a), .b(b), .cin(cin), .s(s), .cout(cout));

    initial begin
        $display("a b cin || s cout");
        $display("--------++-------");

        // テストパターンを試す
        a = 0; b = 0; cin = 0; #10; $display("%d %d  %d  || %d  %d", a, b, cin, s, cout);
        a = 0; b = 0; cin = 1; #10; $display("%d %d  %d  || %d  %d", a, b, cin, s, cout);
        a = 0; b = 1; cin = 0; #10; $display("%d %d  %d  || %d  %d", a, b, cin, s, cout);
        a = 0; b = 1; cin = 1; #10; $display("%d %d  %d  || %d  %d", a, b, cin, s, cout);
        a = 1; b = 0; cin = 0; #10; $display("%d %d  %d  || %d  %d", a, b, cin, s, cout);
        a = 1; b = 0; cin = 1; #10; $display("%d %d  %d  || %d  %d", a, b, cin, s, cout);
        a = 1; b = 1; cin = 0; #10; $display("%d %d  %d  || %d  %d", a, b, cin, s, cout);
        a = 1; b = 1; cin = 1; #10; $display("%d %d  %d  || %d  %d", a, b, cin, s, cout);
    end
endmodule