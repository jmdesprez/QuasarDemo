import co.paralleluniverse.fibers.Fiber
import co.paralleluniverse.fibers.Suspendable
import co.paralleluniverse.kotlin.concurrent.*

fun main(args: Array<String>) {

    object : Fiber<Int>() {
        @Suspendable override fun run(): Int? {
            println("ok!!!")
            return 18
        }
    }.start()


}