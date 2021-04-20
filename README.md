# 346assignment3
final operating systems class coding assignment

classes:
-main: driver class 
-clock: used to simulate time, for synchronization
-page: memory object, holds string ID and value
-outputtofile: writes given string to file
-mailbox: static class containing command queue sent from processes to mmu
-process: simulates OS processes, reads commands.txt and sends command strings to mmu through mailbox
-scheduler: FIFO process scheduler, reads processes.txt for setup (number of cores, processes and process specifications)
-manager: memory manager, performs API calls received from mailbox (store/lookup/release) on memory (page list and vm.txt), reads memconfig.txt for setup
