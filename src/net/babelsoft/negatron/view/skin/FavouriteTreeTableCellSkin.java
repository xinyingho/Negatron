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
package net.babelsoft.negatron.view.skin;

import com.sun.javafx.scene.control.skin.BehaviorSkinBase;
import com.sun.javafx.scene.control.skin.TreeTableCellSkin;
import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.TreeTableCell;
import net.babelsoft.negatron.view.behavior.FavouriteTreeTableCellBehavior;

/**
 *
 * @author capan
 */
public class FavouriteTreeTableCellSkin<S,T> extends TreeTableCellSkin<S,T> {
    
    public FavouriteTreeTableCellSkin(TreeTableCell<S,T> treeTableCell) {
        super(treeTableCell);
        
        try {
            Field f = BehaviorSkinBase.class.getDeclaredField("behavior");
            f.setAccessible(true);
            f.set(this, new FavouriteTreeTableCellBehavior<>(treeTableCell));
        } catch (Exception ex) {
            Logger.getLogger(FavouriteTreeTableCellSkin.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
