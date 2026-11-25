package dev.opencode.mobile.custom.selection

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.opencode.mobile.R
import dev.opencode.mobile.ui.components.OpenCodeButton
import dev.opencode.mobile.ui.components.OpenCodeButtonContrast
import dev.opencode.mobile.ui.components.OpenCodeButtonVariant
import dev.opencode.mobile.ui.icons.OpenCodeIcons

/** Панель выделения в архиве: снять с архива или удалить пачкой. */
@Composable
fun ArchivedSelectionTopBar(
    count: Int,
    onUnarchive: () -> Unit,
    onDelete: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val enabled = count > 0
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OpenCodeButton(
                variant =
                    OpenCodeButtonVariant.IconCircle(
                        OpenCodeIcons.Close,
                        stringResource(R.string.custom_selection_close),
                    ),
                contrast = OpenCodeButtonContrast.Low,
                onClick = onClose,
            )
            Text(
                text = stringResource(R.string.custom_selection_count, count),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 8.dp),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OpenCodeButton(
                    variant =
                        OpenCodeButtonVariant.IconCircle(
                            OpenCodeIcons.Unarchive,
                            stringResource(R.string.archived_sessions_unarchive),
                        ),
                    enabled = enabled,
                    contrast = OpenCodeButtonContrast.Low,
                    onClick = onUnarchive,
                )
                OpenCodeButton(
                    variant =
                        OpenCodeButtonVariant.IconCircle(
                            OpenCodeIcons.Delete,
                            stringResource(R.string.common_delete),
                        ),
                    enabled = enabled,
                    contrast = OpenCodeButtonContrast.Low,
                    onClick = onDelete,
                )
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}
