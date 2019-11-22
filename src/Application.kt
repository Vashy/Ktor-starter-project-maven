package it

import com.fasterxml.jackson.databind.SerializationFeature
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.application.log
import io.ktor.features.ContentNegotiation
import io.ktor.features.StatusPages
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.jackson.jackson
import io.ktor.response.respond
import io.ktor.routing.*
import io.ktor.server.jetty.EngineMain
import javax.management.openmbean.KeyAlreadyExistsException

fun main(args: Array<String>): Unit = EngineMain.main(args)

//@Suppress("unused")
@JvmOverloads
fun Application.module(testing: Boolean = false) {

    install(ContentNegotiation) {
        jackson { enable(SerializationFeature.INDENT_OUTPUT) }
    }

    install(Routing)

    install(StatusPages) {
        exception<Throwable> {
            call.respond(BadRequest, ErrorResponse(it))
        }
    }

    routing {
        trace { application.log.trace(it.buildText()) }

        ordini()
    }
}

fun Routing.ordini() {

    route("/orders") {

        get {
            call.respond(orders)
        }

        get("/{id}") {
            val order = orders.find { call.parameters["id"] == it.number }
            order?.let { call.respond(order) } ?: call.respond(NotFound)
        }

        post<Order>("") { request ->
            if (request.number in orders.map { it.number })
                throw KeyAlreadyExistsException()

            orders += request
        }
    }
}

private val orders = mutableListOf<Order>()

data class Order(
        val number: String,
        val kind: String = "RISCATTO PARZIALE"
)

data class ErrorResponse(val error: String, val stackTrace: List<StackTraceElement>) {
    constructor(cause: Throwable, stackSize: Int = 3) : this(cause.localizedMessage, cause.stackTrace.toList().subList(0, stackSize))
}
