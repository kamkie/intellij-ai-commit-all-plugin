/*
 * Copyright 2026 DevOps Solutions Kamil Kiewisz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pl.devopssolutions.aicommitall.notifications

import com.intellij.notification.Notification
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project

@Service(Service.Level.PROJECT)
internal class AiCommitAllNotificationService(private val project: Project) {
    fun notifyWarning(
        title: String,
        content: String,
    ): Notification = notification(title, content, NotificationType.WARNING)

    fun notifyError(
        title: String,
        content: String,
    ): Notification = notification(title, content, NotificationType.ERROR)

    private fun notification(
        title: String,
        content: String,
        type: NotificationType,
    ): Notification = NotificationGroupManager.getInstance()
        .getNotificationGroup(GROUP_ID)
        .createNotification(title, content, type)
        .also { notification -> notification.notify(project) }

    companion object {
        const val GROUP_ID: String = "AI Commit All"

        fun getInstance(project: Project): AiCommitAllNotificationService = project.service()
    }
}
