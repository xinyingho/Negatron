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
package net.babelsoft.negatron.io.configuration;

import java.util.ArrayList;
import java.util.List;
import javafx.scene.control.TreeItem;
import net.babelsoft.negatron.model.favourites.Favourite;
import net.babelsoft.negatron.model.favourites.Folder;

/**
 *
 * @author capan
 */
public class FavouriteTree {
    
    private TreeItem<Favourite> root;
    private final List<Folder> folderList;
    private final List<Favourite> emptyFavouriteList;
    
    public FavouriteTree() {
        folderList = new ArrayList<>();
        emptyFavouriteList = new ArrayList<>();
    }

    public TreeItem<Favourite> getRoot() {
        return root;
    }

    public void setRoot(TreeItem<Favourite> root) {
        this.root = root;
    }

    public List<Folder> getFolderList() {
        return folderList;
    }
    
    public void addFolder(Folder folder) {
        folderList.add(folder);
    }
    
    public List<Favourite> getEmptyFavouriteList() {
        return emptyFavouriteList;
    }

    public void addEmptyFavourite(Favourite favourite) {
        emptyFavouriteList.add(favourite);
    }
}
