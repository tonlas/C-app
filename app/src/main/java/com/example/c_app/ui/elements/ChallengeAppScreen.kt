package com.example.c_app.ui.elements

import android.annotation.SuppressLint
import android.view.ViewTreeObserver
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.c_app.Challenge
import com.example.c_app.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Composable
fun Challenge(challenge: Challenge) {

    val name = remember {
        mutableStateOf(challenge.name)
    }
    val calendar = remember {
        mutableStateOf(challenge.calendar)
    }
    val checkState = remember {
        mutableStateOf(calendar.value.lastCheckedDateIsToday())
    }
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(1.dp)
            .border(2.dp, Color.Black, RoundedCornerShape(13.dp))
            .padding(10.dp)

    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(end = 10.dp)
                .weight(8f)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {

                val focusManager = LocalFocusManager.current
                val isKeyboardOpen by keyboardAsState()
                if (!isKeyboardOpen) {
                    focusManager.clearFocus()
                }
                OutlinedTextField(

                    value = name.value,
                    onValueChange = { it: String -> name.value = it },
                    textStyle = TextStyle(
                        fontSize = 20.sp
                    ),
                    shape = RoundedCornerShape(13.dp),
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = Color.Transparent
                    ),
                    modifier = Modifier.weight(7f),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = { focusManager.clearFocus() },
                    ),

                    )

                Checkbox(
                    checked = checkState.value,
                    onCheckedChange = { state ->

                        calendar.value.edit(state)
                        checkState.value = state

                    },
                    modifier = Modifier
                        .widthIn(min = 20.dp)
                        .padding(start = 8.dp)
                        .weight(1f)
                        .scale(1.6f)

                )
            }

            Text(
                text = if (checkState.value) {
                    pluralStringResource(
                        R.plurals.current_streak,
                        calendar.value.currentStreak,
                        calendar.value.currentStreak
                    )
                } else {
                    pluralStringResource(
                        R.plurals.max_streak, calendar.value.maxStreak, calendar.value.maxStreak
                    )
                },
                fontSize = 12.sp,
                fontFamily = FontFamily.Serif,
                modifier = Modifier.padding(start = 20.dp, top = 4.dp)
            )
        }


    }
}

@Composable
fun keyboardAsState(): State<Boolean> {
    val view = LocalView.current
    var isImeVisible by remember { mutableStateOf(false) }

    DisposableEffect(LocalWindowInfo.current) {
        val listener = ViewTreeObserver.OnPreDrawListener {
            isImeVisible = ViewCompat.getRootWindowInsets(view)
                ?.isVisible(WindowInsetsCompat.Type.ime()) == true
            true
        }
        view.viewTreeObserver.addOnPreDrawListener(listener)
        onDispose {
            view.viewTreeObserver.removeOnPreDrawListener(listener)
        }
    }
    return rememberUpdatedState(isImeVisible)
}

@Composable
fun DialogWithEditTextField(
    onDismissRequest: () -> Unit,
    onConfirmation: (String) -> Unit,
) {
    val text = rememberSaveable { mutableStateOf("") }
    Dialog(onDismissRequest = { onDismissRequest() }) {
        // Draw a rectangle shape with rounded corners inside the dialog
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 175.dp)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),

            shape = RoundedCornerShape(13.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(15.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {

                OutlinedTextField(value = text.value,
                    onValueChange = { it: String ->
                        text.value = it
                    },
                    shape = RoundedCornerShape(13.dp),
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = Color.Transparent
                    ),
                    label = { Text(text = stringResource(id = R.string.label_description_for_creating_new_challenge)) })
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    TextButton(
                        onClick = { onDismissRequest() },
                        modifier = Modifier.padding(8.dp),
                    ) {
                        Text(text = stringResource(id = R.string.dismiss_to_create_new_challenge))
                    }
                    TextButton(
                        onClick = { onConfirmation(text.value) },
                        modifier = Modifier.padding(8.dp),
                    ) {
                        Text(text = stringResource(id = R.string.confirm_to_create_new_challenge))
                    }
                }
            }
        }
    }
}

@SuppressLint("CoroutineCreationDuringComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> SwipeToDeleteContainer(
    item: T, onDelete: (T) -> Unit, animationDuration: Int = 500, content: @Composable (T) -> Unit
) {

    var isRemoved by remember {
        mutableStateOf(false)
    }

    val coroutineScope = rememberCoroutineScope()
    val state = rememberSwipeToDismissBoxState(confirmValueChange = { value ->
        if (value == SwipeToDismissBoxValue.EndToStart) {
            coroutineScope.launch {
                isRemoved = true
                delay(animationDuration.toLong())
                onDelete(item)
            }
            true
        } else false
    })

    LaunchedEffect(key1 = isRemoved) {
        if (isRemoved) {
            isRemoved = false
            state.reset()
        }
    }

    AnimatedVisibility(
        visible = !isRemoved,
        enter = scaleIn(animationSpec = tween(animationDuration)),
        exit = shrinkVertically(
            animationSpec = tween(animationDuration), shrinkTowards = Alignment.Top
        ) + fadeOut()
    ) {
        SwipeToDismissBox(
            state = state,
            backgroundContent = {

                val color =
                    if (state.dismissDirection == SwipeToDismissBoxValue.EndToStart) Color.Red else Color.Transparent
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(id = R.string.icon_when_challenge_is_deleting),
                        tint = if (state.dismissDirection == SwipeToDismissBoxValue.EndToStart) Color.White else Color.Transparent,
                        modifier = Modifier.padding(end = 7.dp)
                    )
                }
            },
            content = { content(item) },
        )
    }
}

@Composable
fun MyEventListener(onEvent: (event: Lifecycle.Event) -> Unit) {

    val eventHandler = rememberUpdatedState(newValue = onEvent)
    val lifecycleOwner = rememberUpdatedState(newValue = LocalLifecycleOwner.current)

    DisposableEffect(lifecycleOwner.value) {
        val lifecycle = lifecycleOwner.value.lifecycle
        val observer = LifecycleEventObserver { _, event ->
            eventHandler.value(event)
        }

        lifecycle.addObserver(observer)

        onDispose {
            lifecycle.removeObserver(observer)
        }
    }
}


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Preview(showBackground = true)
@Composable
fun ChallengeAppScreen(viewModel: AppViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    val challenges = viewModel.challenges
    val showAlertDialog = remember {
        mutableStateOf(false)
    }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    Scaffold(snackbarHost = {
        SnackbarHost(hostState = snackbarHostState) { data ->
            // custom snackbar with the custom border
            // correct form of
            Snackbar(

                snackbarData = data
            )
        }
    }, bottomBar = {
        BottomAppBar(
            modifier = Modifier.padding(5.dp)
            // add color
        ) {

        }
    }, floatingActionButton = { //add padding
        FloatingActionButton(modifier = Modifier.padding(5.dp), onClick = {
            showAlertDialog.value = true
        }) {
            Icon(Icons.Filled.Add, "Floating action button for adding new challenge.")
            if (showAlertDialog.value) {
                DialogWithEditTextField(onDismissRequest = { showAlertDialog.value = false },
                    onConfirmation = {
                        showAlertDialog.value = false
                        viewModel.addNewChallenge(it)
                    })
            }
        }
    }, floatingActionButtonPosition = FabPosition.EndOverlay
    ) { it ->

        LazyColumn(
            modifier = Modifier.padding(bottom = it.calculateBottomPadding())
        ) {
            items(items = challenges, key = { it.toString() }) {
                SwipeToDeleteContainer(item = it, onDelete = { deletedChallenge ->
                    viewModel.removeChallenge(deletedChallenge)
                    scope.launch {
                        val result = snackbarHostState.showSnackbar(
                            message = context.resources.getString(R.string.snackbar_message_after_challenge_was_deleted),
                            actionLabel = context.resources.getString(R.string.snackbar_action_after_challenge_was_deleted),
                            duration = SnackbarDuration.Short
                        )
                        when (result) {
                            SnackbarResult.ActionPerformed -> {
                                delay(500L)
                                viewModel.undoRemoving()
                            }

                            SnackbarResult.Dismissed -> {
                            }
                        }
                    }
                }) { challenge ->
                    Challenge(challenge)
                }
            }
        }
        MyEventListener {
            when (it) {
                Lifecycle.Event.ON_CREATE -> {
                    scope.launch {
                        viewModel.readFromStorage(context)
                    }
                }

                Lifecycle.Event.ON_PAUSE -> {
                    scope.launch {
                        viewModel.saveInStorage(context)
                    }
                }

                else -> {}
            }
        }
    }
}
