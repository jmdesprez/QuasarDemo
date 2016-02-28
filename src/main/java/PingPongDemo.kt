/*
 * Quasar: lightweight threads and actors for the JVM.
 * Copyright (c) 2015-2016, Parallel Universe Software Co. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation
 *
 *   or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 3.0
 * as published by the Free Software Foundation.
 */
@file:Suppress("PackageDirectoryMismatch")

package actors

import co.paralleluniverse.actors.*
import co.paralleluniverse.fibers.Suspendable
import java.util.concurrent.TimeUnit
import co.paralleluniverse.kotlin.Actor
import co.paralleluniverse.kotlin.*
import co.paralleluniverse.actors.LocalActor

/**
 * @author circlespainter
 * @author pron
 */

// This example is meant to be a translation of the canonical
// Erlang [ping-pong example](http://www.erlang.org/doc/getting_started/conc_prog.html#id67347).

data class Msg(val txt: String, val i: Int, val from: ActorRef<Any?>)

class Ping(val n: Int) : Actor() {
    @Suspendable override fun doRun() {
        val pong: ActorRef<Any> = ActorRegistry.getActor("pong")
        var ready = false
        for(i in 1..n) {
            pong.send(Msg("ping", i, self()))          // Fiber-blocking
            receive {                               // Fiber-blocking, always consume the message
                when (it) {
                    is Msg -> {
                        with(it) {
                            println("Ping received $txt $i from ${from.name}")
                            if(i == 3 && !ready){
                                println("defer")
                                ready = true
                                pong.send("LOL")
                            }
                        }
                    }
                    else -> null                    // Discard
                }
            }
        }
        pong.send("finished")                       // Fiber-blocking
        println("Ping exiting")
    }
}

class Pong() : Actor() {
    @Suspendable override fun doRun() {
        while (true) {
            // snippet Kotlin Actors example
            receive(1000, TimeUnit.MILLISECONDS) {  // Fiber-blocking
                when (it) {
                    is Msg -> {
                        with(it) {
                            if (txt == "ping") {
                                from.send(Msg("pong", i, self())) // Fiber-blocking
                            }
                        }
                    }
                    "finished" -> {
                        println("Pong received 'finished', exiting")
                        return                      // Non-local return, exit actor
                    }
                    is Timeout -> {
                        println("Pong timeout in 'receive', exiting")
                        return                      // Non-local return, exit actor
                    }
                    else -> {
                        println("Pong defer")
                        defer()
                    }
                }
            }
            // end of snippet
        }
    }
}

fun main(args: Array<String>) {
    val pong = spawn(register("pong", Pong()))
    val ping = spawn(Ping(3))
    LocalActor.join(pong)
    LocalActor.join(ping)
}