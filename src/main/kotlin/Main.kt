import java.io.FileWriter

fun main() {
    runServerAndClients(countOfClients = 1)

    runServerAndClients(countOfClients = 1000)
}

private fun runServerAndClients(countOfClients: Int) {
    val writer = FileWriter("results/1_server_${countOfClients}_clients.txt")

    val times = mutableListOf<Long>()
    repeat(COUNT_OF_ITERATIONS) {
        val timeNano = program(countOfClients)
        times.add(timeNano)

        writer.writeWithNewLine("------------------------------")
        writer.writeWithNewLine("Iteration: ${it + 1}")
        writer.writeWithNewLine("Number of clients: $countOfClients")
        writer.writeWithNewLine("Duration nanos: $timeNano")
    }

    writer.writeWithNewLine("------------RESULTS-----------")
    writer.writeWithNewLine("Number of clients: $countOfClients")
    writer.writeWithNewLine("Number of iterations: $COUNT_OF_ITERATIONS")
    writer.writeWithNewLine("Min duration nanos: ${times.min()}")
    writer.writeWithNewLine("Max duration nanos: ${times.max()}")
    writer.writeWithNewLine("Average duration nanos: ${times.average()}")
    writer.writeWithNewLine("Average duration millis: ${times.average() / 1_000_000}")
    writer.flush()
}

private fun FileWriter.writeWithNewLine(data: String) {
    write(data)
    appendLine()
}
