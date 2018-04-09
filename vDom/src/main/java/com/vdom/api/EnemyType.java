package com.vdom.api;

import com.vdom.core.Expansion;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Kevin on 05/03/2018.
 */

public enum EnemyType {

    Goblin("Goblin Horde", Expansion.Base),
    Lizard("Fire Lizard Brigade", Expansion.Base),
    Elf("Fallen Elves", Expansion.Base),
    Winter("Winter of Death", Expansion.Base);



    private final String name;
    private final List<Expansion> expansions;
    EnemyType(String name, Expansion... expansions) {
        this(name, Arrays.asList(expansions));
    }
    EnemyType(String name, List<Expansion> expansions) {
        this.name = name;
        this.expansions = expansions;
    }

    public String getName() {
        return name;
    }

    public List<Expansion> getExpansions() {
        return expansions;
    }
}
