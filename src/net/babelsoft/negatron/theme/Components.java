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

import java.util.List;
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
        String filename = switch (machineComponent) {
            case Device device -> switch (device.getType()) {
                case "cartridge" -> switch (device.getTag()) {
                    // MD
                    case "mdslot:rom_sk:subslot" -> "megadriv_cart.rom_sk.png";
                    case "mdslot:rom_ggenie:subslot" -> "megadriv_cart.rom_ggenie.png";
                    // SNES
                    case "snsslot:bsxrom:bs_slot" -> "bspack.bsxrom.png";
                    case "snsslot:lorom_sgb:gb_slot" -> "gameboy_cart.lorom_sgb.png";
                    case "snsslot:lorom_sufami:st_slot1", "snsslot:lorom_sufami:st_slot2" -> "snes_cart.lorom_sufami.png";
                    // GG
                    case "slot:mgear:subslot" -> "sms_cart.mgear.png";
                    default -> "media-cartridge.png";
                };
                case "yamahaminicart", "cartridge60pin", "card" -> "media-cartridge.png"; // yamahaminicart & cartridge60pin -> Yamaha cx5m128, card -> Sega SMS card
                case "harddisk", "sasihd", "ssd", "winchester", "disk" -> "drive-harddisk.png"; // ssd  -> psion3mx & psionwa, winchester -> Apollo DN3000, disk -> NuBus Disk Image Pseudo-Card
                case "cdrom" -> "media-optical.png";
                case "floppydisk" -> switch (device.getInterfaceFormats()) {
                    case List l when l.contains("floppy_3_5") -> "media-floppy_3_5.png";
                    case List l when l.contains("floppy_8") -> "media-floppy_8.png";
                    case List l when l.contains("floppy_3") -> "media-floppy_3_5.png"; // Amstrad CPC464+, PCW10, SC-3000/Super Control Station SF-7000, ZX Spectrum +3e 8bit IDE, Oric Telestrat
                    default -> "media-floppy_5_25.png"; // floppy_5_25
                };
                case "cassette", "microtape", "ctape" -> "media-tape.png"; // microtape -> hp85
                case "printout", "printer" -> "printer.png"; // "printer" until MAME v0.184, "printout" from MAME v0.185 forward
                case "quickload" -> "application-x-trash.png";
                // serial -> Sharp's PC-E220 & PC-G850V, port -> HP48GX, connect -> connection link of a TIPI card (TI to Raspberry PI hdd replacement), bitbanger -> hp85, rt1715, datacast, sitcom 
                case "serial", "midiin", "midiout", "parallel", "port", "connect", "bitbanger" -> "audio-input-line.png";
                case "memcard" -> "media-flash-memory-stick.png"; // Neo-Geo, PlayStation
                case "snapshot" -> "digikam.png"; // Aleste 520EX, Apple I, BestZX, Blic, Primo B-64
                case "cylinder" -> "media-cylinder.png"; // Magnetically-Coated Cylinder: All Purpose Electronic X-ray Computer (as described in 1957), PDP-1
                case "punchtape", "punchcard" -> "media-punchtape.png"; // Tape Puncher/Reader (reels instead of punchcards) & Card Puncher/Reader
                case "magtape", "tape" -> "media-magtape.png"; // Magnetic tape: Apollo DN3000 & TI Model 990/10 Minicomputer System, tape -> sun3_80
                // Individual ROM image - the Amstrad CPC has a few applications that were sold on 16kB ROMs: ROM Box [cpc_rom], prom -> intlc44, bubble -> Grid Compass 1101
                case "romimage", "promimage", "bubble", "datapack" -> "media-flash.png"; // datapack -> Psion Organiser
                case "picture" -> "image-x-generic.png"; // Sony dfs500, indy_4610
                case "vidfile" -> "video-x-generic.png"; // indy_4610
                case "node_id" -> "view-certificate.png"; // Apollo DN3000
                default -> null;
            };
            case Slot slot -> switch (slot.getName()) {
                case String s when s.contains("ctrl") || s.contains("joy") -> "input-gaming.png";
                case String s when s.startsWith("ext") -> "audio-input-line.png";
                case String s when s.endsWith("kbd") -> "input-keyboard.png";
                case String s when s.startsWith("tape") -> "tape.png"; // c64 datassette
                default -> "audio-card.png";
            };
            case Bios bios -> "media-flash.png";
            default -> "ram.png"; // Ram
        };
        
        Image image = null;
        if (filename != null)
            image = new Image( Components.class.getResourceAsStream(imagePath + filename) );
        return new ImageView(image);
    }

    public static Label loadLabel(MachineComponent<?, ?> machineComponent) {
        return new Label(
            /*Resource.Manager.tryGetString(*/machineComponent.getName()/*)*/
        );
    }
}
