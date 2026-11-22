package com.example.notificationsapp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import com.example.notificationsapp.data.NotificationData
import com.example.notificationsapp.data.NotificationImportance

@Composable
fun NotificationFormScreen(
    onSendNotification: (NotificationData) -> Unit,
    modifier: Modifier = Modifier
) {
    var title by rememberSaveable {
        mutableStateOf("")
    }

    var message by rememberSaveable {
        mutableStateOf("")
    }

    var ongoing by rememberSaveable {
        mutableStateOf(false)
    }

    var importance by rememberSaveable {
        mutableStateOf(NotificationImportance.DEFAULT)
    }

    var menuExpanded by remember {
        mutableStateOf(false)
    }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(dimensionResource(R.dimen.screen_padding)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.form_spacing))
    ) {
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text(stringResource(R.string.notification_title_label)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = message,
            onValueChange = { message = it },
            label = { Text(stringResource(R.string.notification_message_label)) },
            minLines = MESSAGE_MIN_LINES,
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .toggleable(
                    value = ongoing,
                    role = Role.Checkbox,
                    onValueChange = { ongoing = it }
                )
                .padding(vertical = dimensionResource(R.dimen.checkbox_row_padding)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = ongoing,
                onCheckedChange = null
            )

            Text(
                text = stringResource(R.string.notification_ongoing_label),
                modifier = Modifier.padding(start = dimensionResource(R.dimen.checkbox_label_spacing))
            )
        }

        Box {
            OutlinedButton(
                onClick = { menuExpanded = true }
            ) {
                Text(
                    stringResource(
                        R.string.notification_importance_label,
                        stringResource(importance.labelRes)
                    )
                )
            }

            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = {
                    menuExpanded = false
                }
            ) {
                NotificationImportance.entries.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Text(stringResource(option.labelRes))
                        },
                        onClick = {
                            importance = option
                            menuExpanded = false
                        }
                    )
                }
            }
        }

        Button(
            onClick = {
                val notificationData = NotificationData(
                    title = title,
                    message = message,
                    ongoing = ongoing,
                    importance = importance
                )

                onSendNotification(notificationData)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.send_notification))
        }
    }
}
private const val MESSAGE_MIN_LINES = 3
