# Negatron by BabelSoft

## PURPOSE

Negatron is yet another front-end for MAME, the well-known multi-system emulator.

The production of Negatron began on July 7th, 2015, because of the inability of any existing front-ends at the time to entirely unlock all the features MAME has to offer, especially since it has completely merged with its sister project MESS in v0.162, which features a dynamic system of contextual configuration of the emulated machines: the slot expansion system.

So Negatron has been engineered to assist users in managing the huge list of machines available in MAME as intuitively as possible, them being arcade machines, consoles or computers. It has been developed in order to be as much hassle-free as possible, checking for any changed or updated resources automatically.

Moreover, Negatron's second objective is to ease the creation of a complete information set dealing with those video games emulated by MAME, using drag and drop operations, and display it in a convenient way. This information set should archive a whole breadth of resources, helping in preserving what is known as the video game history.

## PREREQUISITES

Negatron requires a Java development kit (JDK) to work: ensure that your computer has at least Java 11, or newer. If you have any doubts, you can download the latest version of OpenJDK at https://jdk.java.net/ or the latest version of Java SE at https://java.oracle.com/ and install it on your computer.

Negatron also requires MAME to be installed on your computer. Although it has only been tested with v0.162 or newer, Negatron should work with MAME / MESS v0.70 and newer as well. You can get the latest version for your specific OS at http://www.mamedev.org. If you plan on using MAME v0.186 or newer, we strongly recommend you to use the derivative NegaMAME instead as recent versions of MAME disabled access to some information on emulated consoles and computers to third-party front-ends. It's available at http://www.babelsoft.net/products/negamame.htm. However, if you only play arcade games, you shouldn't see any differences between official MAME and NegaMAME.

Finally, while not mandatory, Negatron also needs VLC media player 3.0.0 or newer to be installed on your computer in order to play video previews. For this to work, VLC's bitness must match the bitness of the JRE you run with, i.e. if you run Negatron with Java 64-bit, you must install VLC 64-bit as well.
You can get the latest version for your specific OS at http://www.videolan.org. Be aware that the site lets you download the Windows 32-bit version by default.

The latest version of Negatron should always be available at http://www.babelsoft.net/products/negatron.htm.

## QUICK START

Uncompress Negatron's cross-platform zip archive file wherever you see fit, then:
* under Windows, launch Negatron.cmd
* under Linux, launch Negatron.sh
* under MacOS X, launch Negatron.command
You can also use the native installers provided for Windows and macOS.

## SEE ALSO

For any further information, please refer to the manual (Negatron Manual vXXX.pdf).

## LICENSE

Copyright (C) 2015-2020 BabelSoft S.A.S.U.

This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.

## COMPILING NEGATRON

The source code to NegaMAME, Negatron and all its dependencies are available at https://github.com/xinyingho/.

To compile NegaMAME:
1. Download its source code using git i.e. `git clone https://github.com/xinyingho/NegaMAME`.
2. Check out the branch of the version of NegaMAME you want to build e.g. `git checkout negamame0198` for v0.198.
3. Check that your repository is really in the good state by doing `git describe --dirty`. If this command doesn't exactly display the name of the desired tag (usually \<branch name\>-1, e.g. negamame0198-1), then there's an issue e.g. you may need to force download the tags with `git pull --all --tags --prune`.
4. Follow the instructions available at http://docs.mamedev.org/initialsetup/compilingmame.html as NegaMAME has just minor modifications compared to standard MAME.

To compile Negatron:
1. Create a root folder locally that will hold the source code of Negatron and all its dependencies.
2. Within this root folder, download the source code of Negatron, Negatron-Preloader, Negatron-Bootstrap and OpenViewerFX using git. For OpenViewerFX, you must get it from https://github.com/xinyingho/ as this version includes important modifications not available from the original repository of this project.
3. Ensure that you have the latest version of OpenJDK or Oracle Java SE, at least v11. This won't work on earlier versions of Java.
4. The following instructions have been tested against Apache Netbeans 11.2. But it should also work with other IDEs.
5. Open all the 3 Java projects in Netbeans. *Do not import them*, simply use "File > Open Projects...". Netbeans may issue some warnings. Ignore them as they will be solved by compiling the projects.
6. Compile them in this order and you're done:
   1. OpenViewerFX
   2. Negatron-Preloader
   3. Negatron

On the last compilation step, you may encounter an error about not finding the program `jpackage` during the create-package stage. It's normal: as Negatron uses an early access feature of Java for packaging, the official packs are generated using a custom hacked version of this feature.
Negatron-Bootstrap doesn't need to be compiled. This obsolete project is still there to provide the icons for the Windows and macOS installers.
