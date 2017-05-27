package ru.ifmo.ctddev.zyulyaev.compiler

import com.beust.jcommander.JCommander
import com.beust.jcommander.Parameter

/**
 * @author zyulyaev
 * @since 27.05.17
 */
fun main(argv: Array<String>) {
    val args = Args()
    val jCommander = JCommander.newBuilder()
            .addObject(args)
            .build()
    jCommander.parse(*argv);

    when {
        args.interpret -> {}
        args.stackMachine -> {}
        args.compile -> {}
        else -> println(jCommander.usage());
    }
}

class Args {
    @Parameter(description = "<File to interpret/compile>")
    var file: String? = ""

    @Parameter(names = arrayOf("-i"), description = "Interpret mode")
    var interpret = false

    @Parameter(names = arrayOf("-s"), description = "Interpret in stack machine mode")
    var stackMachine = false

    @Parameter(names = arrayOf("-o"), description = "Compile mode")
    var compile = false
}
