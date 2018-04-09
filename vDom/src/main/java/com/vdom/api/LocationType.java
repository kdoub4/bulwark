package com.vdom.api;

import com.vdom.core.Expansion;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Kevin on 05/03/2018.
 */



public enum LocationType {



    Briggs("Fort Briggs", Expansion.Base),
    Citadel("Citadel of Xorjith", Expansion.Base),
    Holtvaros("Holtvaros", Expansion.Base);

    private final String name;
    private final List<Expansion> expansions;
    LocationType(String name, Expansion... expansions) {
        this(name, Arrays.asList(expansions));
    }
    LocationType(String name, List<Expansion> expansions) {
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
