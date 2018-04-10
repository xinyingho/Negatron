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
package net.babelsoft.negatron.io;

/*import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.media.AudioClip;
import javafx.scene.media.MediaException;
import javafx.util.Duration;*/

/**
 * http://translate.google.com/translate_tts?q=[text]&tl=[lang iso alpha-2]
 * @author capan
 */
public class Audio {
    
    public enum Sound {
        INTRO,
        LAUNCH,
        MACHINE_SETTINGS,
        BACK_FROM_MAME,
        SOUND_ON,
        ERROR;
    }
    /*
    private static AudioClip previousClip;
    
    private static int turnedSoundOnCount = 0;
    
    private static Timeline turnedSoundOnTimer = new Timeline(
        new KeyFrame(Duration.seconds(4), event -> turnedSoundOnCount = 0)
    );
    */
    private Audio() { }
    
    public static void play(Sound type) {
        /*String resource = null;
        switch (type) {
            case INTRO:
                resource = "intro/Welcome to Negatron.en-GB";
                break;
            case LAUNCH:
                resource = "launch/Enjoy.en-GB";
                break;
            case MACHINE_SETTINGS:
                double r = Math.random();
                if (r < 0.33334)
                    resource = "machineSettings/Machine settings available.en-GB";
                else if (r < 0.66667)
                    resource = "machineSettings/Some parameters are assigned to this machine.en-GB";
                else
                    resource = "machineSettings/This machine is configurable.en-GB";
                break;
            case BACK_FROM_MAME:
                r = Math.random();
                if (r < 0.33334)
                    resource = "backFromMame/Welcome back.en-GB";
                else if (r < 0.66667)
                    resource = "backFromMame/Another good game yet.en-GB";
                else
                    resource = "backFromMame/Cheers to retrogaming.en-GB";
                break;
            case SOUND_ON:
                if (turnedSoundOnCount == 0 || turnedSoundOnCount % 5 != 0) {
                    r = Math.random();
                    if (r < 0.5)
                        resource = "soundOn/At your service.en-GB";
                    else
                        resource = "soundOn/Ready anytime.en-GB";
                } else
                    resource = "soundOn/rambling/Are you done.en-GB";
                ++turnedSoundOnCount;
                turnedSoundOnTimer.playFromStart();
                break;
            case ERROR:
                resource = "error/Oh dear!.en-GB";
                break;
        }
        
        if (resource != null) {
            if (previousClip != null)
                previousClip.stop();
            
            try {
                AudioClip clip = new AudioClip(Audio.class.getResource(
                    "/net/babelsoft/negatron/resource/audio/" + resource + ".mp3"
                ).toExternalForm());
                clip.play();
                previousClip = clip;
            } catch (MediaException ex) {
                Logger.getLogger(Audio.class.getName()).log(Level.SEVERE, null, ex);
                // swallow error
            }
        }*/
    }
}
