# Scala チートシート

## 変数定義
Chisel で定義する回路ハードウェアは全て ```val``` で定義

```scala
// 再代入可能な var
var variableName = some_value

// 再代入不可能な val
val variableName = some_value
```

## 関数定義
処理内容が1行の場合、右辺の波括弧は省略可

```scala
// 無名関数
(arg: arg_type) => { /* 処理内容 */ }

// 名前を付けることもできる
val functionName = (arg: arg_type) => { /* 処理内容 */ }
```

## メソッド定義
処理内容が1行の場合、右辺の波括弧は省略可

```scala
// 返り値の型指定なし
def methodName(arg: arg_type) = { /* 処理内容 */ }

// 返り値の型指定なし
def methodName(arg: arg_type): return_type = { /* 処理内容 */ }

// 複数のパラメータリストを取る場合
def methodName(arg1: arg1_type)(arg2: arg2_type): return_type = { /* 処理内容 */ }
```

## コレクション: ```Seq```
0から順にインデックスされた順番を持ち、基本的には immutable (値の変更不可)

```scala
// Seq の宣言
val seqName = Seq(1, 2, 3)

// インデックスで要素にアクセス
seqName(0)
```

### ```tabulate``` メソッド
第1引数に要素数、第2引数に関数を指定し、0から連続する製図鵜に対して関数を適用した結果を格納した ```Seq``` を返す

```scala
val seqName = Seq.tabulate(element_num)(/* 関数 */)
```

### ```reverse``` メソッド
```Seq``` の中身を逆順に並び替える

```scala
seqName.reverse
```

## ```for``` 文
Scala では ```for``` 文を式として扱う

```scala
// 基本構文
for (i <- start to end) {/* 処理内容 */}

// Seq の繰り返し処理
for (i <- seqInst) {/* 処理内容 */}

// 条件付きの構文
for (i <- start to end if(/* 条件式 */)) {/* 処理内容 */}

// 二重ループ
for (x <- start_x to end_x; y <- start_y to end_y) {/* 処理内容 */}
```

## オブジェクト
### クラス
継承できるクラスは1つだけ

```scala
// クラス定義
class ClassName(var param1: param1_type, ...) {
    /* クラスの実装 */ 
}

// class の継承
class ChildClass extends ParentClass {
    /* クラスの実装 */ 
}
```

### トレイト
トレイトは単体ではインスタンス化できないが、クラスの機能を切り分けて実装し、```with``` 句を使って複数継承できる

```scala
// トレイトの定義
trait TraitName1 {/* トレイトの実装 */}
trait TraitName2 {/* トレイトの実装 */}

// 継承
class ClassName extends TraitName1 with TraitName2 {
    /* クラスの実装 */ 
}
```

### シングルトンオブジェクト
1つしかインスタンスを生成できないクラスに相当する

```scala
object SingletonObject {/* 実装 */}
```

ファクトリーメソッドを持つコンパニオンオブジェクトとして利用される

```scala
// クラスの定義
class Example (arg: arg_type) {/* 実装 */}

// ファクトリーメソッドを持つコンパニオンオブジェクトの定義
object Example {
    def apply(arg: arg_type) = {
        new Example(arg)
    }
}

// インスタンスの生成、apply は省略可能
val inst = Example.apply(arg)
val inst = Example(arg)

// コンストラクタ引数を private 化して直接インスタンスを生成できないようにすることも可能
class Example private (arg: arg_type) {/* 実装 */}
```

Chisel では ```apply``` メソッドでインスタンスを生成する手法がよく用いられる

## 名前空間
### ```package```
各ファイルの先頭で指定される名前空間

```scala
// package 宣言
package hoge

class Fuga {...}

// ほかのパッケージからの参照
val piyo = new hoge.Fuga
```

### ```import```
```import``` 句で指定された ```package``` のメンバにアクセスできるようになる

```scala
// package 内のすべてのメンバをインポートするには _ が必要
import hoge._

val piyo = new Fuga
```