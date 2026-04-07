# Day 2. (前半) EDK II 入門
UEFI やその周辺プログラムの開発キットとして公開されている EDK II を使って、"Hello, world" プログラムを作り直してみます。
また、EDK II の機能を用いてパソコンのメモリマップを取得してみます。

## EDK II の導入とツールの構成
まずは、EDK II を GitHub リポジトリからクローンしてきます。

```bash
$ cd mikanos
$ git clone https://github.com/tianocore/edk2
```

```edk2``` ディレクトリの中には、次のようなディレクトリやファイルが含まれています。

- ```edksetup.sh```  
  環境変数の設定用スクリプト
- ```Conf/target.txt```  
  ビルド対象を設定するテキストファイル (初期状態では存在しない)
- ```Conf/tools_def.txt```  
  ビルドで使用するコンパイラを設定するテキストファイル (初期状態では存在しない)
- ```MdePkg/```  
  基本ライブラリを格納したディレクトリ
- ```AppPkg/```  
  UEFI アプリケーションを含むライブラリを格納したディレクトリ
- ```OvmfPkg/```  
  OVMF (オープンソース実装の UEFI BIOS) が格納されたディレクトリ


## EDK II で Hello World
MikanOS のブートローダとして "Hello World" を表示するプログラムを作成します。
```MikanLoaderPkg``` ディレクトリの中に、次のファイルを配置します。

- ```MikanLoaderPkg.dec```: パッケージ宣言ファイル
- ```MikanLoaderPkg.dsc```: パッケージ記述ファイル
- ```Loader.inf```: モジュール情報ファイル
- ```Main.c```: ソースコード

### [```MikanLoaderPkg.dec```](../../mikanos/MikanLoader/MikanLoaderPkg.dec)
パッケージ宣言ファイルは、パッケージの名前を中心にバージョン情報などを記述するファイルです。
基本的に ```PACKAGE_NAME``` 以外は変更せずにそのまま使います。

```dec
[Defines]
  DEC_SPECIFICATION              = 0x00010005
  PACKAGE_NAME                   = MikanLoaderPkg
  PACKAGE_GUID                   = 452eae8e-71e9-11e8-a243-df3f1ffdebe1
  PACKAGE_VERSION                = 0.1
```

### [```MikanLoaderPkg.dsc```](../../mikanos/MikanLoader/MikanLoaderPkg.dsc)
パッケージ記述ファイルはいくつかのセクションに分かれています。
```Defines``` セクションには次のような設定項目があります。

```dsc
[Defines]
  PLATFORM_NAME                  = MikanLoaderPkg
  PLATFORM_GUID                  = d3f11f4e-71e9-11e8-a7e1-33fd4f7d5a3e
  PLATFORM_VERSION               = 0.1
  DSC_SPECIFICATION              = 0x00010005
  OUTPUT_DIRECTORY               = Build/MikanLoader$(ARCH)
  SUPPORTED_ARCHITECTURES        = X64
  BUILD_TARGETS                  = DEBUG|RELEASE|NOOPT
```

```SUPPORTED_ARCHITECTURES``` には、対象となる CPU アーキテクチャを記述します。
ここでは、```X64``` を指定していますが、```ARM``` や ```IA32``` なども指定可能です。
```OUTPUT_DIRECTORY``` には、ビルドした ```.efi``` ファイルの出力先を指定します。
```$(ARCH)``` で CPU アーキテクチャの名前が入るようになっています。

```LibraryClasses``` セクションには、```<ライブラリ名>|<ライブラリの情報モジュールへのパス>``` という形式で、使用するライブラリを列挙します。

```dsc
[LibraryClasses]
  UefiApplicationEntryPoint|MdePkg/Library/UefiApplicationEntryPoint/UefiApplicationEntryPoint.inf
  UefiLib|MdePkg/Library/UefiLib/UefiLib.inf
```

```Components``` セクションには、ビルド対象のコンポーネントを指定します。
ここでは、次に説明する ```Loader.inf``` を指定しています。

```dsc
[Components]
  MikanLoaderPkg/Loader.inf
```

### [```Loader.inf```](../../mikanos/MikanLoader/Loader.inf)
EDK II のパッケージは複数のモジュールを持つことができ、モジュールごとにモジュール情報ファイルを作成する必要があります。

```inf
[Defines]
  INF_VERSION                    = 0x00010006
  BASE_NAME                      = Loader
  FILE_GUID                      = c9d0d202-71e9-11e8-9e52-cfbfd0063fbf
  MODULE_TYPE                    = UEFI_APPLICATION
  VERSION_STRING                 = 0.1
  ENTRY_POINT                    = UefiMain

#  VALID_ARCHITECTURES           = X64

[Sources]
  Main.c

[Packages]
  MdePkg/MdePkg.dec

[LibraryClasses]
  UefiLib
  UefiApplicationEntryPoint

[Guids]

[Protocols]
```

まず、```Defines``` セクションの ```BASE_NAME``` にモジュール名を設定します。
また、```ENTRY_POINT``` には UEFI アプリケーションのエントリポイント名を書きます。
エントリポイントとは、起動時に最初に実行される関数である、通常の C/C++ プログラムの ```main``` 関数に該当します。
```Sources``` セクションにはモジュールを構成するソースコードを、```PackageSection``` にはビルドするのに必要なパッケージを、```LibraryClasses``` には、パッケージ記述ファイルの ```LibraryClasses``` に指定したライブラリを、それぞれ列挙します。

### [```Main.c```](../../mikanos/MikanLoader/Main,c)
今回は、ここに "Hello, world!" を表示するプログラムを記述します。
通常の C/C++ プログラムでは、最初に呼び出される関数の名前は ```main``` 関数で固定されていますが、EDK II はアプリケーションごとに自由に設定できます。

```c
#include <Uefi.h>
#include <Library/UefiLib.h>

EFI_STATUS EFIAPI UefiMain(EFI_HANDLE image_handle, EFI_SYSTEM_TABLE *system_table) {
    Print(L"Hello, world!\n");
    while(1);
    return EFI_SUCCESS;
}
```

```Uefi.h``` や ```Library/UefiLib.h``` に実体は、```edk2/MdePkg/Include/``` ディレクトリにあります。
```EFI_HANDLE``` 型や ```EFI_SYSTEM_TABLE``` 型、```Print``` 関数は、これらのファイルに実装されています。
```Print``` 関数は C 言語標準の ```printf``` 関数に似た文字列表示関数です。

### ソースコードのビルドと表示
それでは、```Main.c``` を EDK II を使ってビルドしてみます。
まずは、```edk2``` ディレクトリの直下に、先ほど実装した ```MikanLoaderPkg``` のシンボリックリンクを作成します。

```bash
$ cd $HOME/mikanos/edk2/
$ ln -s $HOME/computer_architecture/mikanos/MikanLoaderPkg ./
```

次に、```source``` コマンドで ```edksetup.sh``` を読み込みます。
```edksetup.sh``` は ```edk2``` ディレクトリにデフォルトで置いてあるスクリプトで、実行すると ```Conf/target.txt``` ファイルが生成されます。

```bash
$ source edksetup.sh
```

```Conf/target.txt``` を開いて、以下の設定を記述します。

```txt
ACTIVE_PLATFORM = MikanLoaderPkg/MikanLoaderPkg.dsc
TARGET = DEBUG
TARGET_ARCH = X64
TOOL_CHAIN_TAG = CLANG38
```

ここで、EDK II の ```build``` コマンドを実行してみましょう。
```build``` コマンドは、先ほどの ```edksetup.sh``` を読み込んだときに使えるようになっています。
```build``` が成功すると、```mikanos/edk2/Build/MikanLoaderX64/DEBUG_CLANGPDB/X64/``` に ```Loader.efi``` が生成されます。
これを ```run_qemu.sh``` で読み込んで実行すると、前回と同じように QEMU で "Hello, world!" が表示されます。

```bash
$ ./devenv/run_qemu.sh edk2/Build/MikanLoaderX64/DEBUG_CLANGPDB/X64/Loader.efi
```

### エラー対応
参考書の記述そのままではエラーが多発して動かなかったため、次のような変更を加えています。

#### 必要なパッケージのインストール
最初に環境構築した際にインストールしたパッケージだけでは足りなかったため、以下のパッケージをインストールしておきます。

```bash
$ sudo apt install make lldb llvm nasm g++ build-essential uuid-dev
```

#### EDK II のサブモジュール取得
EDK II をクローンしただけでは取得しきれないサブモジュールを参照しているようで、クローンだけだとエラーになります。

```
edk2/MdePkg/MdePkg.dec(33): error 000E: File/directory not found in workspace edk2/MdePkg/Library/MipiSysTLib/mipisyst/library/include
```

追加で下記のコマンドを実行する必要がありました。

```
git submodule update --init --recursive
```

#### ```MikanLoaderPkg.dsc``` の変更
```build``` を実行すると、```StackCheckLib``` や ```RegisterFilterLib``` がないと怒られます。

```
error 4000: Instance of library class [StackCheckLib] is not found for module
error 4000: Instance of library class [RegisterFilterLib] is not found for module
```

DSC ファイルに以下を追記します。

```
[LibraryClasses]
  ...
  StackCheckLib|MdePkg/Library/StackCheckLibNull/StackCheckLibNull.inf
  RegisterFilterLib|MdePkg/Library/RegisterFilterLibNull/RegisterFilterLibNull.inf
```

#### ```Conf/target.txt``` の変更
参考書の p.51 では、```TOOL_CHAIN_TAG``` に ```CLANG38``` を指定していますが、Clang の古いバージョンにしか対応していません。
下記の通り ```CLANGPDB``` に変更しています。

#### 追加コマンド
これはどういう効果があるのよくわかっていませんが、ChatGPT で1行目の ```make``` コマンドを実行するようにと言われたので入れたところ、うまくいきました。

```bash
$ make -C BaseTools
$ source edksetup.sh
$ build
```
