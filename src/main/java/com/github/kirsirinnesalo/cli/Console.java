package com.github.kirsirinnesalo.cli;

import java.util.function.Consumer;

public class Console {

    private Console() {}

    private static Consumer<Object> console = System.out::println;

    public static void printLine(Object text) {
        console.accept(String.valueOf(text));
    }

    public static void printEmptyLine() {
        printLine("");
    }

}
