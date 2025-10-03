package com.example.calculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.calculator.ui.theme.CalculatorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CalculatorTheme {
                val viewModel = viewModel<CalculatorViewModel>()
                CalculatorScreen(state = viewModel.state, onAction = viewModel::onAction)
            }
        }
    }
}

@Composable
fun CalculatorScreen(state: CalculatorState, onAction: (CalculatorAction) -> Unit) {
    val buttonSpacing = 10.dp
    var isScientificPanelVisible by remember { mutableStateOf(false) }

    val backgroundColor = Color(0xFF0A2647)
    val textColor = Color.White

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(horizontal = buttonSpacing)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = state.expression,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            textAlign = TextAlign.End,
            fontSize = 22.sp,
            color = textColor.copy(alpha = 0.6f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        val displayFontSize = when (state.display.length) {
            in 0..8 -> 90.sp
            in 9..12 -> 70.sp
            else -> 50.sp
        }

        Text(
            text = state.display,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            textAlign = TextAlign.End,
            fontSize = displayFontSize,
            color = textColor,
            fontWeight = FontWeight.Light,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Clip
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isScientificPanelVisible = !isScientificPanelVisible },
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isScientificPanelVisible) "▼ BASIC" else "▲ SCIENTIFIC",
                color = textColor.copy(alpha = 0.7f),
                fontSize = 14.sp
            )
        }
        Spacer(modifier = Modifier.height(buttonSpacing))

        AnimatedVisibility(visible = isScientificPanelVisible) {
            ScientificPanel(state = state, buttonSpacing = buttonSpacing, onAction = onAction)
        }

        NumericPanel(state = state, buttonSpacing = buttonSpacing, onAction = onAction)
        Spacer(modifier = Modifier.height(buttonSpacing))
    }
}

@Composable
fun ScientificPanel(state: CalculatorState, buttonSpacing: Dp, onAction: (CalculatorAction) -> Unit) {
    val functionColor = Color(0xFF205295)
    val inverseColor = Color(0xFF7C96AB)
    val layout = listOf(
        listOf("Inv", "log", "ln", "x!"),
        listOf("√", "sin", "cos", "tan"),
        listOf("1/x", "%", "+/-", "xʸ")
    )
    Column {
        layout.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(buttonSpacing)
            ) {
                row.forEach { symbol ->
                    val color = if (symbol == "Inv" && state.isInverse) inverseColor else functionColor
                    CalculatorButton(
                        symbol,
                        state,
                        Modifier.weight(1f).aspectRatio(1f),
                        color,
                        onAction
                    )
                }
            }
            Spacer(modifier = Modifier.height(buttonSpacing))
        }
    }
}

@Composable
fun NumericPanel(state: CalculatorState, buttonSpacing: Dp, onAction: (CalculatorAction) -> Unit) {
    val numberColor = Color(0xFF144272)
    val operatorColor = Color(0xFF2C74B3)
    val functionColor = Color(0xFF205295)
    val layout = listOf(
        listOf("AC", "(", ")", "÷"),
        listOf("7", "8", "9", "×"),
        listOf("4", "5", "6", "-"),
        listOf("1", "2", "3", "+"),
        listOf("DEL", "0", ".", "=")
    )
    Column {
        layout.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(buttonSpacing)
            ) {
                row.forEach { symbol ->
                    val isWide = symbol == "0"
                    val weight = if (isWide) 2f else 1f
                    val color = when (symbol) {
                        "AC", "DEL", "(", ")" -> functionColor
                        "÷", "×", "-", "+", "=" -> operatorColor
                        else -> numberColor
                    }
                    CalculatorButton(
                        symbol,
                        state,
                        Modifier.weight(weight).aspectRatio(if (isWide) 2f else 1f),
                        color,
                        onAction
                    )
                }
            }
            Spacer(modifier = Modifier.height(buttonSpacing))
        }
    }
}

fun getActionForSymbol(symbol: String, isInverse: Boolean): CalculatorAction {
    return when (symbol) {
        "Inv" -> CalculatorAction.Inverse
        "√" -> CalculatorAction.SquareRoot
        "log" -> CalculatorAction.Log
        "ln" -> CalculatorAction.Ln
        "x!" -> CalculatorAction.Factorial
        "1/x" -> CalculatorAction.Reciprocal
        "xʸ" -> CalculatorAction.PowerOf
        "sin" -> CalculatorAction.Sin(inverse = isInverse)
        "cos" -> CalculatorAction.Cos(inverse = isInverse)
        "tan" -> CalculatorAction.Tan(inverse = isInverse)
        "AC" -> CalculatorAction.AllClear
        "DEL" -> CalculatorAction.Clear
        "(" -> CalculatorAction.ParenthesisOpen
        ")" -> CalculatorAction.ParenthesisClose
        "+/-" -> CalculatorAction.ToggleSign
        "." -> CalculatorAction.Decimal
        "=" -> CalculatorAction.Calculate
        "+", "-", "÷", "×", "%" -> CalculatorAction.Operation(symbol.first())
        else -> symbol.toIntOrNull()?.let { CalculatorAction.Number(it) } ?: CalculatorAction.Norm
    }
}

@Composable
fun CalculatorButton(
    symbol: String,
    state: CalculatorState,
    modifier: Modifier,
    color: Color,
    onAction: (CalculatorAction) -> Unit
) {
    val displayText = when {
        symbol == "sin" && state.isInverse -> "sin⁻¹"
        symbol == "cos" && state.isInverse -> "cos⁻¹"
        symbol == "tan" && state.isInverse -> "tan⁻¹"
        else -> symbol
    }
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(color)
            .clickable { onAction(getActionForSymbol(symbol, state.isInverse)) },
        contentAlignment = if (symbol == "0") Alignment.CenterStart else Alignment.Center
    ) {
        Text(
            text = displayText,
            fontSize = 20.sp,
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(start = if (symbol == "0") 25.dp else 0.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewScientificCalculatorScreen() {
    CalculatorTheme {
        CalculatorScreen(
            state = CalculatorState(display = "123", expression = "25+3"),
            onAction = {}
        )
    }
}
