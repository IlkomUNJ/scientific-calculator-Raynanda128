package com.example.calculator

sealed class CalculatorAction {
    data class Number(val number: Int) : CalculatorAction()
    data class Operation(val operator: Char) : CalculatorAction()
    object Clear : CalculatorAction()
    object AllClear : CalculatorAction()
    object Decimal : CalculatorAction()
    object Calculate : CalculatorAction()
    object ToggleSign : CalculatorAction()
    object ParenthesisOpen : CalculatorAction()
    object ParenthesisClose : CalculatorAction()
    object Norm : CalculatorAction()
    object Inverse : CalculatorAction()
    object Reciprocal : CalculatorAction()
    object Factorial : CalculatorAction()
    object SquareRoot : CalculatorAction()
    object Log : CalculatorAction()
    object Ln : CalculatorAction()
    object PowerOf : CalculatorAction()
    data class Sin(val inverse: Boolean = false) : CalculatorAction()
    data class Cos(val inverse: Boolean = false) : CalculatorAction()
    data class Tan(val inverse: Boolean = false) : CalculatorAction()
}