# Day 2. (後半) メモリマップ
前回は、EDK II を使って Hello, world！を表示するプログラムを作成しました。
今回は、UEFI の機能を使ってメモリマップを取得するプログラムを実装していきます。

## メインメモリとメモリマップ
パソコンに搭載されているメインメモリには複数のメモリチップが搭載されていますが、ソフトウェア的には多数のバイトが隙間なく直線状に整列しているように見えます。
それぞれのバイトには0から順番に連番のアドレス (番地) が振られており、アドレスを指定することで CPU からメインメモリのデータにバイト単位でアクセスできます。

メモリマップは、メインメモリのどの領域が何に使われているのかを表す「地図」のような役割を果たします。
例えば、以下のようなメモリマップを考えてみましょう。

| PhysicalStart | Type | NumberOfPages |
| :--: | :--: | :--: |
| 0x00000000 | EfiBootServicesCode | 0x1 |
| 0x00001000 | EfiConventionalMemory | 0x9F |
| 0x00100000 | EfiConventionalMemory | 0x700 |
| 0x00800000 | EfiACPIMemoryNVS | 0x8 |
| $vdots$ | $vdots$ | $vdots$ |

メモリマップの各行は、```PhysicalStart``` を先頭の番地とする大きさ ```NumberOfPages``` メモリ領域を表しています。
```NumberOfPages``` はページ単位で表現されています。
1ページのサイズは文脈によって異なりますが、UEFI のメモリマップでは 4KiB を指します。
```Type``` はメモリの用途を示しており、以下のような種類があります。

| Type 値 | Type 名 | 意味 |
| :--: | :--: | :-- |
| 1 | EfiLoaderCode | UEFI アプリケーションの実行コード |
| 2 | EfiLoaderData | UEFI アプリケーションが使うデータ領域 |
| 3 | EfiServicesCode | ブートサービスドライバの実行コード |
| 4 | EfiServicesData | ブートサービスドライバが使うデータ領域 |
| 7 | EfiConvensionalMemory | 空き領域 |

## メモリマップを取得するコードの実装
```Main.c``` にメモリマップを取得する関数 ```GetMemoryMap``` を定義します。
UEFI で OS の起動に必要な機能を提供するブートサービスを格納したグローバル変数 ```gBS``` の ```GetMemoryMap``` メソッドを呼び出してメモリマップを取得しています。

```c
EFI_STATUS GetMemoryMap(struct MemoryMap* map) {
    if (map->buffer == NULL) {
        return EFI_BUFFER_TOO_SMALL;
    }

    // グローバル変数 gBS の GetMemoryMap メソッドを呼び出す
    map->map_size = map->buffer_size;
    return gBS->GetMemoryMap(
        &map->map_size,
        (EFI_MEMORY_DESCRIPTOR*)map->buffer,
        &map->map_key,
        &map->descriptor_size,
        &map->descriptor_version
    );
}
```

```GetMemoryMap``` メソッドの引数と戻り値は以下の通りです。
```IN``` や ```OUT``` は　EDK II の独自マクロで、関数への入出力を明示的に表現できるようになっています。

```c
EFI_STATUS GetMemoryMap(
    IN OUT UINTN *MemoryMapSize,
    IN OUT EFI_MEMORY_DESCRIPTOR *MemoryMap,
    OUT UINTN *MapKey,
    OUT UINTN *DescriptorSize,
    OUT UINT32 *DescriptorVersion
);
```

| 引数 | 説明 |
| :-- | :-- |
| MemoryMapSize | メモリマップ書き込み用のメモリサイズを設定して渡し、実際のメモリマップの大きさがセットされる |
| MemoryMap | メモリマップの書き込み先の先頭アドレス |
| MapKey | メモリマップを識別する値の格納先 |
| DescriptorSize | メモリマップの各行を表すメモリディスクリプタのサイズの格納先 |
| DescriptorVersion | メモリディスクリプタの構造体のバージョン |

メモリディスクリプタ構造体の各フィールドは以下の通りです。

| フィールド名 | 型 | 説明 |
| :-- | :-- | :-- |
| Type | UINT32 | メモリ領域の種別 |
| PhysicalStart | EFI_PHYSICAL_ADDRESS | メモリ領域の先頭の物理メモリアドレス |
| VirtualStart | EFI_VIRTUAL_ADDRESS | メモリ領域先頭の仮想メモリアドレス |
| NumberOfPages | UINT64 | メモリ領域の大きさ (4KiB ページ単位) |
| Attribute | UINT64 | メモリ領域が使える用途を示すビット集合 |

実装したプログラムでは、```gBS->GetMemoryMap``` で取得できる情報と、メモリディスクリプタを保持するための構造体 ```MemoryMap``` を独自に定義しています。

```c
struct MemoryMap {
    UINTN buffer_size;
    VOID* buffer;
    UINTN map_size;
    UINTN map_key;
    UINTN descriptor_size;
    UINT32 descriptor_version;
};
```

取得したメモリマップは、```OpenRootDir``` 関数や ```SaveMemoryMap``` 関数を用いてファイルに書き出します。
具体的には、CSV 形式でヘッダとメモリディスクリプタの中身をファイルに書き込んでいます。

```c
EFI_STATUS SaveMemoryMap(struct MemoryMap* map, EFI_FILE_PROTOCOL* file) {
    CHAR8 buf[256];
    UINTN len;

    // ヘッダの書き出し
    CHAR8* header = "Index, Type, Type(name), PhysicalStart, NumberOfPages, Attribute\n";
    len = AsciiStrLen(header);
    file->Write(file, &len, header);

    Print(L"map->buffer = %08lx, map->map_size = %08lx\n", map->buffer, map->map_size);

    // 構造体に格納された情報の書き出し
    EFI_PHYSICAL_ADDRESS iter;
    int i;
    for (
        iter = (EFI_PHYSICAL_ADDRESS)map->buffer, i = 0;
        iter < (EFI_PHYSICAL_ADDRESS)map->buffer + map->map_size;
        iter += map->descriptor_size, i++
    ) {
        EFI_MEMORY_DESCRIPTOR* desc = (EFI_MEMORY_DESCRIPTOR*)iter;
        len = AsciiSPrint(
            buf, sizeof(buf),
            "%u, %x, %-ls, %08lx, %lx, %lx\n",
            i, desc->Type, GetMemoryTypeUnicode(desc->Type),
            desc->PhysicalStart, desc->NumberOfPages,
            desc->Attribute & 0xffffflu
        );
        file->Write(file, &len, buf);
    }

    return EFI_SUCCESS;
}
```

## メモリマップの確認
上記で実装したコードをビルドして実行してみます。

```bash
$ cd mikanos/edk2
$ source edksetup.sh
$ build
```

上記のコマンドを実行すると、```lld-link: error: undefined symbol: gEfiLoadedImageProtocolGuid``` というエラーが出ました。
ChatGPT に聞くと、```MikanLoaderPkg``` の ```Loader.inf``` に以下を追記せよとのことでした。

```inf
[Protocols]
  gEfiLoadedImageProtocolGuid
  gEfiSimpleFileSystemProtocolGuid
```

これで ```build``` コマンドを再実行すると、ビルドが通りました。
```run_qemu.sh``` を実行すると、```disk.img``` の中に ```memmap``` というファイルが生成されます。

```bash
# QEMU を起動
$ cd ../
$ ./devenv/run_qemu.sh edk2/Build/MikanLoaderX64/DEBUG_CLANGPDB/X64/Loader.efi

# メモリマップを書き出したファイルの確認
$ sudo mkdir -p /mnt/mikanos_disk
$ sudo mount -o loop mikanos/devenv/disk.img /mnt/mikanos_disk
$ cat /mnt/mikanos_disk/memmap
```

最後のコマンドを実行すると、以下のようにメモリマップが書き出されていることを確認できます。

```csv
Index, Type, Type(name), PhysicalStart, NumberOfPages, Attribute
0, 3, EfiBootServicesCode, 00000000, 1, F
1, 7, EfiConventionalMemory, 00001000, 9F, F
2, 7, EfiConventionalMemory, 00100000, 700, F
3, A, EfiACPIMemoryNVS, 00800000, 8, F
4, 7, EfiConventionalMemory, 00808000, 8, F
5, A, EfiACPIMemoryNVS, 00810000, F0, F
6, 4, EfiBootServicesData, 00900000, B00, F
7, 7, EfiConventionalMemory, 01400000, 2B36, F
8, 4, EfiBootServicesData, 03F36000, 20, F
9, 7, EfiConventionalMemory, 03F56000, 2752, F
10, 1, EfiLoaderCode, 066A8000, 3, F
11, 4, EfiBootServicesData, 066AB000, 10F, F
12, 9, EfiACPIReclaimMemory, 067BA000, 1, F
13, 4, EfiBootServicesData, 067BB000, C3, F
14, 3, EfiBootServicesCode, 0687E000, B4, F
15, A, EfiACPIMemoryNVS, 06932000, 12, F
16, 0, EfiReservedMemoryType, 06944000, 1C, F
17, 3, EfiBootServicesCode, 06960000, 10A, F
18, 6, EfiRuntimeServicesData, 06A6A000, 7, F
19, 5, EfiRuntimeServicesCode, 06A71000, 5, F
20, 6, EfiRuntimeServicesData, 06A76000, 5, F
21, 5, EfiRuntimeServicesCode, 06A7B000, 5, F
22, 6, EfiRuntimeServicesData, 06A80000, 5, F
23, 5, EfiRuntimeServicesCode, 06A85000, 7, F
24, 6, EfiRuntimeServicesData, 06A8C000, 8F, F
25, 4, EfiBootServicesData, 06B1B000, 725, F
26, 7, EfiConventionalMemory, 07240000, 3, F
27, 4, EfiBootServicesData, 07243000, 6, F
28, 7, EfiConventionalMemory, 07249000, 1, F
29, 4, EfiBootServicesData, 0724A000, 7D1, F
30, 7, EfiConventionalMemory, 07A1B000, 1, F
31, 3, EfiBootServicesCode, 07A1C000, 17F, F
32, 5, EfiRuntimeServicesCode, 07B9B000, 30, F
33, 6, EfiRuntimeServicesData, 07BCB000, 24, F
34, 0, EfiReservedMemoryType, 07BEF000, 4, F
35, 9, EfiACPIReclaimMemory, 07BF3000, 8, F
36, A, EfiACPIMemoryNVS, 07BFB000, 4, F
37, 4, EfiBootServicesData, 07BFF000, 201, F
38, 7, EfiConventionalMemory, 07E00000, 8D, F
39, 4, EfiBootServicesData, 07E8D000, 20, F
40, 3, EfiBootServicesCode, 07EAD000, 20, F
41, 4, EfiBootServicesData, 07ECD000, 9, F
42, 3, EfiBootServicesCode, 07ED6000, 1E, F
43, 6, EfiRuntimeServicesData, 07EF4000, 84, F
44, A, EfiACPIMemoryNVS, 07F78000, 88, F
45, 6, EfiRuntimeServicesData, FFC00000, 400, 1
```
