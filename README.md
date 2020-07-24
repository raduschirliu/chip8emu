# chip8emu
A complete Chip-8 Emulator, with a full debugger and memory viewer, made in Java using LWJGL 3.  
  
<img src="https://i.imgur.com/8xLh3rh.png" width="250" height="250">
<img src="https://i.imgur.com/FiFk7Hc.png" width="250" height="250">
<img src="https://i.imgur.com/1Lpd5Oy.png" width="250" height="250">

## Running the emulator
The repository into Eclipse as a Maven project, and can be ran after installing the necessary
dependencies through Maven.

## How to use
When the emulator is open, press `tab` to choose a ROM. The ROM path can also be supplied as the first and only command line argument.  
After a ROM is loaded, you can press `space` to emulator pause/resume execution, or press the `~` key to open the debugger.

The keyboard keys mappings to the Chip-8 hex keyboard are:
```
Keyboard         Chip-8
-----------------------
1234             123C
qwer    <--->    456B
asdf             789E
zxcv             A0BF
```

## References
A list of references used to learn about the inner workings of Chip-8 and build this project:
- http://devernay.free.fr/hacks/chip8/C8TECH10.HTM#0.0
- https://en.wikipedia.org/wiki/CHIP-8
- https://github.com/dmatlack/chip8
  
A collection of Chip-8 ROMS can be found [here](https://github.com/dmatlack/chip8/tree/master/roms)