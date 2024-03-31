# コンピュータ・アーキテクチャ

CPU・OS の自作をはじめ、低レイヤのお勉強用リポジトリ

## 参考書

### コンピュータアーキテクチャ

- デイビッド・A・パターソン, ジョン・Ｌ・ヘネシー(著), 成田光彰(訳) (2014)『[コンピュータの構成と設計 第 5 版 (上/下)](https://bookplus.nikkei.com/atcl/catalog/14/P98420/)』日経 BP 社
- デイビッド・A・パターソン, アンドリュー・ウォーターマン(著), 成田 光彰(訳) (2018)『[RISC-V 原典](https://bookplus.nikkei.com/atcl/catalog/18/269170/)』日経 BP 社
- 吉瀬謙二 (2024)『[RISC-V で学ぶコンピュータアーキテクチャ完全入門](https://gihyo.jp/book/2024/978-4-297-14008-3)』技術評論社
- ⻄⼭悠太朗，井⽥健太 (2021)『[RISC-V と Chisel で学ぶはじめての CPU 自作](https://gihyo.jp/book/2021/978-4-297-12305-5)』技術評論社
- 井澤裕司 (2019)『[動かしてわかる CPU の作り方 10 講](https://gihyo.jp/book/2019/978-4-297-10821-2)』技術評論社
- 渡波 郁 (2003)『[CPU の創りかた](https://book.mynavi.jp/ec/products/detail/id=22065)』マイナビブックス

### FPGA 開発

- 小林優 (2018)『[FPGA ボードで学ぶ組み込みシステム開発入門](https://gihyo.jp/book/2018/978-4-7741-9388-5)』技術評論社
- 森岡澄夫 (2012)『[LSI・FPGA の回路アーキテクチャ設計法](https://shop.cqpub.co.jp/detail/2611/)』CQ 出版社
- 木村真也 (2006)『[わかる VerilogHDL 入門](https://shop.cqpub.co.jp/hanbai/books/37/37561.htm)』CQ 出版社

### OS 　自作

- 内田公太 (2021)『[ゼロからの OS 自作入門](https://book.mynavi.jp/ec/products/detail/id=121220)』マイナビブックス

## 環境構築

基本的には WSL 上の Ubuntu か Macbook Air M1 上で動かします。

### Verilog・GTKWave

Icarus Verilog と GTKWave をインストールします。

```bash
# Ubuntu
$ sudo apt update
$ sudo apt install iverilog gtkwave

# MacBook Air
$ brew install icarus-verilog gtkwave
```

### Chisel

『[RISC-V と Chisel で学ぶはじめての CPU 自作](https://gihyo.jp/book/2021/978-4-297-12305-5)』の環境構築に従って Docker で環境を作ります。
詳細は `dockerfile.riscv-chisel` を参照してください。

```bash
$ docker build . -t riscv-chisel -f dockerfile.riscv-chisel
```

ビルドに非常に時間がかかるので、Docker Hub からイメージをダウンロードすることもできます。

```bash
$ docker pull yutaronishiyama/riscv-chisel-book:latest
```

コンテナの起動は以下のコマンドで行います。
コンテナの `/src` にマウントするディレクトリは、環境によって読み替えてください。

```bash
$ docker run -it -v ./riscv-chisel:/src riscv-chisel
```

### MikanOS
『[ゼロからの OS 自作入門](https://book.mynavi.jp/ec/products/detail/id=121220)』で制作する MikanOS は x64 環境を想定して設計されているため、ARM ベースの MacBook Air 上では動きません。
そのため、[MikanOS のビルド手順](https://github.com/uchan-nos/mikanos-build/) を参考にWSL 上に環境を構築します。

