package com.example.calculator

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import java.text.DecimalFormat
import kotlin.math.*

data class CalculatorState(
    val display: String = "0",
    val expression: String = "",
    val justCalculated: Boolean = false,
    val isInverse: Boolean = false
)

class CalculatorViewModel : ViewModel() {
    var state by mutableStateOf(CalculatorState())
        private set

    fun onAction(action: CalculatorAction) {
        state = when (action) {
            is CalculatorAction.Number -> handleNumber(state, action.number)
            is CalculatorAction.Operation -> handleOperation(state, action.operator)
            is CalculatorAction.Calculate -> {
                val result = calculateExpression(state.expression)
                state.copy(
                    display = result,
                    expression = state.expression + "=",
                    justCalculated = true
                )
            }
            is CalculatorAction.AllClear -> CalculatorState()
            is CalculatorAction.Clear -> handleDelete(state)
            is CalculatorAction.Decimal -> handleDecimal(state)
            is CalculatorAction.ToggleSign -> handleToggleSign(state)
            is CalculatorAction.Inverse -> state.copy(isInverse = !state.isInverse)
            is CalculatorAction.Reciprocal -> handleReciprocal(state)
            is CalculatorAction.Factorial -> handleFactorial(state)
            is CalculatorAction.SquareRoot -> handleSquareRoot(state)
            is CalculatorAction.Log -> handleLog(state)
            is CalculatorAction.Ln -> handleLn(state)
            is CalculatorAction.PowerOf -> handleOperation(state, '^')
            is CalculatorAction.Sin -> handleTrigonometry(state, "sin", "asin", action.inverse, ::sin, ::asin)
            is CalculatorAction.Cos -> handleTrigonometry(state, "cos", "acos", action.inverse, ::cos, ::acos)
            is CalculatorAction.Tan -> handleTrigonometry(state, "tan", "atan", action.inverse, ::tan, ::atan)
            else -> state
        }
    }

    private fun handleNumber(currentState: CalculatorState, number: Int): CalculatorState {
        return if (currentState.justCalculated) {
            CalculatorState(
                display = formatNumber(number.toString()),
                expression = number.toString()
            )
        } else {
            val raw = currentState.display.replace(".", "").replace(",", "")
            val newRaw = if (raw == "0" || currentState.display == "Error") {
                number.toString()
            } else raw + number

            currentState.copy(
                display = formatNumber(newRaw),
                expression = currentState.expression + number,
                justCalculated = false
            )
        }
    }

    private fun handleDecimal(currentState: CalculatorState): CalculatorState {
        return if (currentState.justCalculated) {
            CalculatorState(display = "0.", expression = "0.")
        } else if (!currentState.display.contains(".")) {
            currentState.copy(
                display = currentState.display + ".",
                expression = currentState.expression + "."
            )
        } else currentState
    }

    private fun handleOperation(currentState: CalculatorState, operation: Char): CalculatorState {
        return if (currentState.justCalculated) {
            currentState.copy(
                expression = currentState.display.replace(".", "").replace(",", "") + operation,
                justCalculated = false
            )
        } else {
            currentState.copy(
                display = "0",
                expression = currentState.expression + operation,
                justCalculated = false
            )
        }
    }

    private fun handleDelete(currentState: CalculatorState): CalculatorState {
        if (currentState.justCalculated) return CalculatorState()
        val raw = currentState.display.replace(".", "").replace(",", "")
        val newRaw = if (raw.length > 1) raw.dropLast(1) else "0"
        val newExpr = if (currentState.expression.isNotEmpty()) {
            currentState.expression.dropLast(1)
        } else ""
        return currentState.copy(
            display = formatNumber(newRaw),
            expression = newExpr
        )
    }

    private fun calculateExpression(expression: String): String {
        return try {
            val sanitized = expression
                .replace("×", "*")
                .replace("÷", "/")

            val result = object : Any() {
                var pos = -1
                var ch: Int = 0
                fun nextChar() { ch = if (++pos < sanitized.length) sanitized[pos].code else -1 }
                fun eat(charToEat: Int): Boolean {
                    while (ch == ' '.code) nextChar()
                    if (ch == charToEat) { nextChar(); return true }
                    return false
                }
                fun parse(): Double {
                    nextChar()
                    val x = parseExpression()
                    if (pos < sanitized.length) throw RuntimeException("Unexpected: ${sanitized[pos]}")
                    return x
                }
                fun parseExpression(): Double {
                    var x = parseTerm()
                    while (true) {
                        x = when {
                            eat('+'.code) -> x + parseTerm()
                            eat('-'.code) -> x - parseTerm()
                            else -> return x
                        }
                    }
                }
                fun parseTerm(): Double {
                    var x = parseFactor()
                    while (true) {
                        x = when {
                            eat('*'.code) -> x * parseFactor()
                            eat('/'.code) -> x / parseFactor()
                            else -> return x
                        }
                    }
                }
                fun parseFactor(): Double {
                    if (eat('+'.code)) return parseFactor()
                    if (eat('-'.code)) return -parseFactor()

                    var x: Double
                    val startPos = pos
                    if (eat('('.code)) {
                        x = parseExpression()
                        eat(')'.code)
                    } else if ((ch in '0'.code..'9'.code) || ch == '.'.code || ch == ','.code) {
                        while ((ch in '0'.code..'9'.code) || ch == '.'.code || ch == ','.code) nextChar()
                        x = sanitized.substring(startPos, pos).replace(",", ".").toDouble()
                    } else {
                        throw RuntimeException("Unexpected: ${ch.toChar()}")
                    }

                    // pangkat ^
                    if (eat('^'.code)) {
                        x = x.pow(parseFactor())
                    }

                    // persen %
                    if (eat('%'.code)) {
                        x = x / 100.0
                    }

                    return x
                }
            }.parse()
            formatNumber(result)
        } catch (e: Exception) {
            "Error"
        }
    }

    private fun handleReciprocal(currentState: CalculatorState): CalculatorState {
        val value = currentState.display.replace(".", "").replace(",", "").toDoubleOrNull() ?: return currentState
        val result = 1.0 / value
        return currentState.copy(
            display = formatNumber(result),
            expression = "1/(${currentState.display})"
        )
    }

    private fun handleSquareRoot(currentState: CalculatorState): CalculatorState {
        val value = currentState.display.replace(".", "").replace(",", "").toDoubleOrNull() ?: return currentState
        val result = sqrt(value)
        return currentState.copy(
            display = formatNumber(result),
            expression = "√(${currentState.display})"
        )
    }

    private fun handleLog(currentState: CalculatorState): CalculatorState {
        val value = currentState.display.replace(".", "").replace(",", "").toDoubleOrNull() ?: return currentState
        val result = log10(value)
        return currentState.copy(
            display = formatNumber(result),
            expression = "log(${currentState.display})"
        )
    }

    private fun handleLn(currentState: CalculatorState): CalculatorState {
        val value = currentState.display.replace(".", "").replace(",", "").toDoubleOrNull() ?: return currentState
        val result = ln(value)
        return currentState.copy(
            display = formatNumber(result),
            expression = "ln(${currentState.display})"
        )
    }

    private fun handleFactorial(currentState: CalculatorState): CalculatorState {
        val value = currentState.display.replace(".", "").replace(",", "").toDoubleOrNull()
        if (value != null && value >= 0 && value % 1.0 == 0.0) {
            var result: Long = 1
            for (i in 1..value.toLong()) result *= i
            return currentState.copy(
                display = formatNumber(result.toDouble()),
                expression = "${currentState.display}!"
            )
        }
        return currentState.copy(display = "Error")
    }

    private fun handleTrigonometry(
        currentState: CalculatorState,
        symbol: String,
        invSymbol: String,
        isInverse: Boolean,
        fn: (Double) -> Double,
        invFn: (Double) -> Double
    ): CalculatorState {
        val value = currentState.display.replace(".", "").replace(",", "").toDoubleOrNull() ?: return currentState
        val result = if (isInverse) Math.toDegrees(invFn(value)) else fn(Math.toRadians(value))
        val symbolUsed = if (isInverse) invSymbol else symbol
        return currentState.copy(
            display = formatNumber(result),
            expression = "$symbolUsed(${currentState.display})"
        )
    }

    private fun handleToggleSign(currentState: CalculatorState): CalculatorState {
        val value = currentState.display.replace(".", "").replace(",", "").toDoubleOrNull() ?: return currentState
        return currentState.copy(display = formatNumber(value * -1))
    }

    private fun formatNumber(value: Any): String {
        return try {
            val df = DecimalFormat("#,###.##########")
            df.isGroupingUsed = true
            df.maximumFractionDigits = 10
            when (value) {
                is Double -> df.format(value)
                is String -> {
                    val parsed = value.replace(".", "").replace(",", "").toDoubleOrNull()
                    if (parsed != null) df.format(parsed) else value
                }
                else -> value.toString()
            }
        } catch (e: Exception) {
            value.toString()
        }
    }
}
