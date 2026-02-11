#include <Uefi.h>
#include <Library/UefiLib.h>

EFI_STATUS EFIAPI UefiMain()
{
    Print(L"Hello, Mikan World!\n");
    while(1);
    return EFI_SUCCESS;
}