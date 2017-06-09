package ru.ifmo.ctddev.zyulyaev.compiler.asm;

/**
 * @author zyulyaev
 * @since 08.06.2017
 */
class Header {
    static final int COUNTER_OFFSET = 0;

    static class Array {
        static final int SIZE = 8;
        static final int LENGTH_OFFSET = 4;
    }

    static class Data {
        static final int SIZE = 4;
    }

    static class VirtualTable {
        static final int DESTRUCTOR_OFFSET = 0;
    }
}
