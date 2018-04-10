/*
 * This file is part of Negatron.
 * Copyright (C) 2015-2018 BabelSoft S.A.S.U.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.babelsoft.negatron.theme;

import java.util.function.Consumer;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import net.babelsoft.negatron.model.component.Bios;
import net.babelsoft.negatron.model.component.Device;
import net.babelsoft.negatron.model.component.MachineComponent;
import net.babelsoft.negatron.model.component.Slot;
import net.babelsoft.negatron.model.item.Machine;

/**
 *
 * @author capan
 */
public class Components {
    
    private final static String imagePath = "/net/babelsoft/negatron/resource/icon/device/";
    
    private Components() { }

    public static ImageView loadIcon(Machine machine, MachineComponent<?, ?> machineComponent) {
        final Image[] image = new Image[1];
        Consumer<String> setImage = filename -> image[0] = new Image(
            Components.class.getResourceAsStream(imagePath + filename)
        );
        
        if (machineComponent instanceof Device) {
            Device device = (Device)machineComponent;
            switch (device.getType()) {
                case "cartridge":
                    switch (device.getTag()) {
                        // MD
                        case "mdslot:rom_sk:subslot":
                            setImage.accept("megadriv_cart.rom_sk.png");
                            break;
                        case "mdslot:rom_ggenie:subslot":
                            setImage.accept("megadriv_cart.rom_ggenie.png");
                            break;
                        // SNES
                        case "snsslot:bsxrom:bs_slot":
                            setImage.accept("bspack.bsxrom.png");
                            break;
                        case "snsslot:lorom_sgb:gb_slot":
                            setImage.accept("gameboy_cart.lorom_sgb.png");
                            break;
                        case "snsslot:lorom_sufami:st_slot1":
                        case "snsslot:lorom_sufami:st_slot2":
                            setImage.accept("snes_cart.lorom_sufami.png");
                            break;
                        // GG
                        case "slot:mgear:subslot":
                            setImage.accept("sms_cart.mgear.png");
                            break;
                        default:
                            setImage.accept("media-cartridge.png");
                            break;
                    }
                    break;
                case "harddisk":
                    setImage.accept("drive-harddisk.png");
                    break;
                case "cdrom":
                    setImage.accept("media-optical.png");
                    break;
                case "floppydisk":
                    if (device.getInterfaceFormats().contains("floppy_3_5"))
                        setImage.accept("media-floppy_3_5.png");
                    else if (device.getInterfaceFormats().contains("floppy_8"))
                        setImage.accept("media-floppy_8.png");
                    else if (device.getInterfaceFormats().contains("floppy_3")) // Amstrad CPC464+, PCW10, SC-3000/Super Control Station SF-7000, ZX Spectrum +3e 8bit IDE, Oric Telestrat
                        setImage.accept("media-floppy_3_5.png");
                    else // floppy_5_25
                        setImage.accept("media-floppy_5_25.png");
                    break;
                case "cassette":
                    setImage.accept("media-tape.png");
                    break;
                case "printer":
                case "printout": // new name for "printer" from MAME v0.185 forward
                    setImage.accept("printer.png");
                    break;
                case "quickload":
                    setImage.accept("application-x-trash.png");
                    break;
                case "midiin":
                case "midiout":
                case "serial": // Sharp's PC-E220 & PC-G850V
                    setImage.accept("audio-input-line.png");
                    break;
                case "memcard": // Neo-Geo, PlayStation
                    setImage.accept("media-flash-memory-stick.png");
                    break;
                case "snapshot": // Aleste 520EX, Apple I, BestZX, Blic, Primo B-64
                    setImage.accept("digikam.png");
                    break;
                case "cylinder": // Magnetically-Coated Cylinder: All Purpose Electronic X-ray Computer (as described in 1957), PDP-1
                    setImage.accept("media-cylinder.png");
                    break;
                /*case "punchcard": // Card Puncher/Reader
                    break;*/
                case "punchtape": // Tape Puncher/Reader (reels instead of punchcards): All Purpose Electronic X-ray Computer (as described in 1957), PDP-1
                    setImage.accept("media-punchtape.png");
                    break;
                case "magtape": // Magnetic tape: Apollo DN3000, TI Model 990/10 Minicomputer System
                    setImage.accept("media-magtape.png");
                    break;
                case "romimage": // Individual ROM image - the Amstrad CPC has a few applications that were sold on 16kB ROMs: ROM Box [cpc_rom]
                    setImage.accept("media-flash.png");
                    break;
                /*case "parallel": // TI-99 RS232/PIO interface
                    break;*/
            }
        } else if (machineComponent instanceof Slot) {
            String name = ((Slot) machineComponent).getName();
            if (name.contains("ctrl") || name.contains("joy"))
                setImage.accept("input-gaming.png");
            else if (name.startsWith("ext"))
                setImage.accept("audio-input-line.png");
            else if (name.endsWith("kbd"))
                setImage.accept("input-keyboard.png");
            else if (name.startsWith("tape")) // c64 datassette
                setImage.accept("tape.png");
            else
                setImage.accept("audio-card.png");
        } else if (machineComponent instanceof Bios)
            setImage.accept("media-flash.png");
        else // Ram
            setImage.accept("ram.png");
        
        return new ImageView(image[0]);
    }

    public static Label loadLabel(MachineComponent<?, ?> machineComponent) {
        return new Label(
            /*Resource.Manager.tryGetString(*/machineComponent.getName()/*)*/
        );
    }
}
