/*
 * This file is part of Negatron.
 * Copyright (C) 2015-2024 BabelSoft S.A.S.U.
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
package net.babelsoft.negatron.view.skin;

import com.sun.javafx.scene.control.behavior.BehaviorBase;
import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.skin.TreeTableViewSkin;
import net.babelsoft.negatron.model.favourites.Favourite;
import net.babelsoft.negatron.view.behavior.FavouriteTreeViewBehavior;
import net.babelsoft.negatron.view.control.FavouriteTreeView;

/**
 *
 * @author Xiny
 */
public class FavouriteTreeViewSkin extends TreeTableViewSkin<Favourite> {
    
    public FavouriteTreeViewSkin(FavouriteTreeView control) {
        super(control);
        
        try {
            Field f = TreeTableViewSkin.class.getDeclaredField("behavior");
            f.setAccessible(true);
            
            BehaviorBase behavior = (BehaviorBase)f.get(this);
            f.set(this, new FavouriteTreeViewBehavior(control));
            behavior.dispose();
        } catch (Exception ex) {
            Logger.getLogger(FavouriteTreeViewSkin.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
