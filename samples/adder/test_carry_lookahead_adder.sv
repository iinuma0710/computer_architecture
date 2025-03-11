`include "carry_lookahead_adder.sv"

module test_adder_block();
    logic [3:0] a, b;
    logic [3:0] s;
    logic       cin, cout;
    logic [4:0] expected;

    // 4ビット加算器のインスタンスを作成
    adder_block #(.NBIT(4)) _adder (.a(a), .b(b), .cin(cin), .s(s), .cout(cout));

    initial begin
        $display(" a  b cin ||  s cout | OK?");
        $display("----------++---------|----");

        // ランダムに16個のパターンを試す
        repeat(16) begin
            #20;
            a = $random&15;
            b = $random&15;
            cin = $random&1;
        end
    end

    // 結果を表示
    initial forever @(a, b, cin) begin
        #0;
        expected = a + b + cin;
        if (s + cout * 16 == expected)
            $display("%2d %2d  %d  || %2d  %d   | OK", a, b, cin, s, cout);
        else
            $display("%2d %2d  %d  || %2d  %d   | NG", a, b, cin, s, cout);
    end
endmodule


module test_carry_lookahead_adder();
    logic [31:0] a, b;
    logic [31:0] s;
    logic        cout;
    logic [32:0] expected;

    // 32ビット加算器のインスタンスを作成
    carry_lookahead_adder #(.NBIT(32)) _adder (.a(a), .b(b), .s(s), .cout(cout));

    initial begin
        $display("      a          b     ||      s     cout | OK?");
        $display("-----------------------++-----------------|-----");

        // ランダムに16個のパターンを試す
        repeat(16) begin
            #20;
            a = $random&5000000000;
            b = $random&5000000000;
        end
    end

    // 結果を表示
    initial forever @(a, b) begin
        #0;
        expected = a + b;
        if (s + cout * (1 << 32) == expected)
            $display(" %10d %10d || %10d  %d   | OK", a, b, s, cout);
        else
            $display(" %10d %10d || %10d  %d   | NG", a, b, s, cout);
    end
endmodule
