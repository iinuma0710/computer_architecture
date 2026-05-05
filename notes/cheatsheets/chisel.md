# Chisel のチートシート
## bit 値を表す基本型
### 整数型 ```UInt```/```SInt``` オブジェクト
- ```UInt```: 符号なし整数を表す型
- ```SInt```: 符号あり整数を表す型
- ```W```: ビット幅を表す ```Width``` 型を返すメソッド
- ```U```: Scala の整数型 ```Int``` を Chisel の ```UInt``` 型に変換するメソッド (定数の変換)
- ```asUInt```: Scala の整数型 ```Int``` を Chisel の ```UInt``` 型に変換するメソッド (変数の変換)

```scala
// 32bit の配線を定義
val a = UInt(32.W)
val b = SInt(32.W)

// 具体的な値を持つ変数の定義
val c = 2.U(32.W)   // 符号なし整数
val d = 2.U         // ビット幅の自動推定
val e = -2.S(32.W)  // 符号あり整数
val f = -2.S        // ビット幅の自動推定
val g = 2
val h = g.asUInt(32.W)
```

最上位と最下位のビット位置を指定してデータを抽出可能

```scala
val a = "b11000".U
val b = a(3, 0)
```

### Bool 型 ```Bool``` オブジェクト
```true``` または ```false``` の2値を表す信号を生成する

```scala
val a = Bool()
val b = true.B
val c = false.B
```

## 演算子
### 四則演算子
被演算子の型は ```UInt``` または ```SInt``` で、同じ型の返り値が得られる

| 演算子 | 意味 |
| :--: | :--: |
| + | 加算 |
| - | 減算 |
| * | 乗算 |
| / | 除算 |
| % | 剰余 |

### 比較演算子
被演算子の型は ```UInt``` または ```SInt``` で、同じ型の返り値が得られる

| 演算子 | true.B の条件 |
| :--: | :-- |
| a > b | a が b より大きい |
| a >= b | a が b 以上 |
| a < b | a が b より小さい |
| a <= b | a が b 以下 |
| a === b | a と b が等しい |
| a === b | a と b が等しくない |

### 演算子
```Bool``` 型を取る場合と ```bit``` 単位型を取る場合がある

| 演算子 | 被演算子の型 | 意味 |
| :--: | :--: | :--: |
| && | Bool | 論理積 (AND) |
| \|\| | Bool | 論理和 (OR) |
| !  | Bool | 否定 (NOT) |
| & | bit | 論理積 (AND) |
| \| | bit | 論理和 (OR) |
| ~  | bit | 否定 (NOT) |
| ^ | bit | 排他的論理和 (XOR) |

### シフト演算子
左辺は ```UInt```/```SInt``` 型、右辺は ```UInt``` 型を取り、返り値は ```Bits``` 型となる

| 演算子 | true.B の条件 |
| :--: | :-- |
| >> | 右シフト |
| << | 論理左シフト |

左辺が ```UInt``` 型の場合は論理右シフト (空き桁をゼロ埋め)、```SInt``` 型の場合は算術右シフト (空き桁を最上位ビット埋め) となる

## ```Module```
回路を定義するクラスが継承するクラス

```scala
// 回路を定義するクラス
class Hardware extends Module {/* 回路の実装 */}

// インスタンス化
val inst = new Hardware()

// Chisel のハードウェア化、ここでの Module はオブジェクト
val hardware = Module(inst)
```

## ```IO``` オブジェクト
```Module``` クラスで必ず定義する入出力ポート

```scala
class Hardware extends Module {
    val io = IO(new Bundle {
        val input  = Input(Uint(32.U))
        val output = Output(Uint(32.U))
    })
    ...
}
```

### ```Input``` / ```Output``` オブジェクト
それぞれ入力信号と出力信号を定義するオブジェクト

### ```Bundle``` クラス
複数の信号をまとめるクラス

### ```IO``` オブジェクト
Bundle クラスのインスタンスを引数とする入出力ポートの定義

### ```clock``` / ```reset``` 信号
```Module``` クラスを継承したクラスで暗黙的に定義される信号

## ```Flipped``` オブジェクト
引数の ```Bundle``` インスタンスの入出力を反転させるオブジェクトで、IO の受け手と送り手で反転した入出力ポートを簡単に定義できる

```scala
class ExampleIO extends Bundle {
    val a = Output(Uint(32.U))
}

class Sender extends Module {
    val io = IO(new Bundle {
        val x = new ExampleIO()
    })
    ...
}

class Receiver extends Module {
    val io = IO(new Bundle {
        val x = Flipped(new ExampleIO())
    })
    ...
}
```

## 配線の接続
```:=``` は右辺から左辺へ単一の信号を接続し、```<>``` は両辺の各ポートの信号を一括で接続する

```scala
val receiver = Module(new Receiver())
val sender = Module(new Sender())

receiver.io.x.a = sender.io.x.a
receiver.io <> sender.io
```

## 組み合わせ論理回路
接続先が未定の状態で Chisel のハードウェアを確保する必要がある場合には ```Wire``` オブジェクトを使う

```scala
// 接続先は未定
val wireInst = Wire(UInt(32.U))

// 初めから信号が接続されている配線
val wireDefaultInst = WireDefault(0.U(32.W))
```

## 順序論理回路
レジスタを表すオブジェクト

```scala
// 初期値のないレジスタ
val regInst = Reg(32.W)

// 初期値を設定するレジスタ
val regInitInst = RegInit(0.U(32.W))

// ほかの信号を直接引数に取るレジスタ
val otherSignal = true.B
val regNextInst = RegNext(otherSignal)
```

## ```Mem``` オブジェクト
レジスタやメインメモリの実装に使ったり、それらにファイルからデータを読み込んだりすることができる

```scala
// bit_num 幅の UInt 型レジスタを reg_num 本用意する
val regfile = Mem(reg_num, Uint(bit_num.W))

// レジスタ番号を指定してデータを読み出し、書き込み
val read_data = regfile(reg_addr.U)
regfile(reg.addr.U) := /* データ */

// メインメモリの定義
val mem = Mem(mem_size, UInt(bit_num.W))

// メインメモリにファイルからデータを読み込む
import chisel3.util.experimental.loadMemoryFromFile
loadMemoryFromFile(mem, "mem.hex")
```

## 制御回路
### ```BitPat``` オブジェクト
ビットパターンを表現するオブジェクトで、実際のビット列と ```===``` を使って比較する

```scala
// ビットパターンを比較
"b10101".U === BitPat("b10101")

// ? は don't care bit として扱われる
"b10101".U === BitPat("b101??")
```

### ```when``` オブジェクト
ほかのプログラミング言語における ```if``` 文に相当

```scala
when(/* 条件式1 */) {
    // 条件式1が true.B の時の処理
}.elsewhen(/* 条件式2 */) {
    // 条件式2が true.B の時の処理
}.otherwise {
    // それ以外の場合の処理
}
```

### ```switch``` オブジェクト
ほかのプログラミング言語における ```case``` 文に相当

```scala
switch(/* 信号 */) {
    is(/* 値1 */) {
        // 信号 === 値1 の時の処理
    }
    is(/* 値2 */) {
        // 信号 === 値2 の時の処理
    }
}
```

### ```Mux``` オブジェクト
マルチプレクサに相当する処理を行う回路を生成する

```scala
// in === true.B なら out1、それ以外なら out2 を返す
val mux = Mux(in, out1, out2)
```

### ```MuxCase``` オブジェクト
$N$ 入力マルチプレクサに相当する処理を行う回路を生成する

```scala
val a = Mux(defaultValue, Seq(
    condition_1 -> /* condition_1 === true.B の場合に接続する信号 */,
    condition_2 -> /* condition_2 === true.B の場合に接続する信号 */,
    ...
))

// タプル形式で指定してもよい
val a = Mux(defaultValue, Seq(
    (condition_1, /* condition_1 === true.B の場合に接続する信号 */),
    (condition_2, /* condition_2 === true.B の場合に接続する信号 */),
    ...
))
```

### ```ListLookup``` オブジェクト
```addr``` と合致した ```BitPat``` に対応する ```List``` を返す

```scala
ListLookup(addr: UInt, default: List, mapping: Array[(BitPat, List)])
```

命令デコードで使用する例は以下の通り

```scala
val ADD  = BitPat("b0000000??????????000?????0110011")
val ADDI = BitPat("b?????????????????000?????0010011")
val csignals = ListLookup(
    inst,
    List(ALU_X, OP1_RS1, OP2_RS2),
    Array(
        ADD  -> List(ALU_ADD, OP1_RS1, OP2_RS2),
        ADDI -> List(ALU_ADD, OP1_RS1, OP2_IMI),
    )
)

// csignal の各要素を個別の変数に格納する
val exec_func :: op1_sel :: op2_sel :: Nil = csignals
```

## bit 操作
### ```Cat``` オブジェクト
2つのビット列をつなげて1つの連続したビット列に連接する

```scala
Cat("b101".U, "b11".U)
```

### ```Fill``` オブジェクト
特定の要素繰り返した ```UInt``` 型を返す

```scala
Fill(repeat_num: Int, repeated_element: UInt)
```
