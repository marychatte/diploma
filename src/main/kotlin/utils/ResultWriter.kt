package utils

import java.io.FileWriter

class ResultWriter(
    private val timesNanos: List<Long>,
    private val countOfClients: Int,
    private val fileName: String = "results/1_server_${countOfClients}_clients.txt",
    private val countOfIterations: Int = COUNT_OF_ITERATIONS,
) {

    fun write() {
        FileWriter(fileName).use { writer ->
            timesNanos.forEachIndexed { index, timeNano ->
                writer.writeWithNewLine("------------------------------")
                writer.writeWithNewLine("Iteration: ${index + 1}")
                writer.writeWithNewLine("Number of clients: $countOfClients")
                writer.writeWithNewLine("Duration nanos: $timeNano")
            }

            writer.writeWithNewLine("------------RESULTS-----------")
            writer.writeWithNewLine("Number of clients: $countOfClients")
            writer.writeWithNewLine("Number of iterations: $countOfIterations")
            writer.writeWithNewLine("Min duration nanos: ${timesNanos.min()}")
            writer.writeWithNewLine("Max duration nanos: ${timesNanos.max()}")
            writer.writeWithNewLine("Average duration nanos: ${timesNanos.average()}")
            writer.writeWithNewLine("Average duration millis: ${timesNanos.average() / 1_000_000}")
            writer.flush()
        }
    }
}

private fun FileWriter.writeWithNewLine(data: String) {
    write(data)
    appendLine()
}
