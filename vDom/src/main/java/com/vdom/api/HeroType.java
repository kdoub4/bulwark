package com.vdom.api;

import com.vdom.core.Expansion;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Kevin on 05/03/2018.
 */

public enum HeroType {

    None("No One"),

    Arpad("Arpad", Expansion.Base),
    Sandor("Sandor", Expansion.Base),
    ZsuZsa("ZsuZsa", Expansion.Base),
    Wiola("Wiola", Expansion.Base);



    private final String name;
    private final List<Expansion> expansions;
    HeroType(String name, Expansion... expansions) {
        this(name, Arrays.asList(expansions));
    }
    HeroType(String name, List<Expansion> expansions) {
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
