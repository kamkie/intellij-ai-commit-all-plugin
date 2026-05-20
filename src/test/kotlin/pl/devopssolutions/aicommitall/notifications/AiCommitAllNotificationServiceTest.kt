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
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import org.w3c.dom.Element
import java.lang.reflect.Proxy
import java.nio.file.Path
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.io.path.inputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

internal class AiCommitAllNotificationServiceTest {
    @Test
    fun `warning notification is routed through plugin notification group`() {
        val project = testProject()
        val router = CapturingNotificationRouter()
        val service = AiCommitAllNotificationService(project, router)

        val notification = service.notifyWarning(
            title = "Warning title",
            content = "Warning content",
        )

        assertEquals(
            NotificationRequest(
                project = project,
                groupId = AiCommitAllNotificationService.GROUP_ID,
                title = "Warning title",
                content = "Warning content",
                type = NotificationType.WARNING,
            ),
            router.requests.single(),
        )
        assertSame(router.notifications.single(), notification)
    }

    @Test
    fun `error notification is routed through plugin notification group`() {
        val project = testProject()
        val router = CapturingNotificationRouter()
        val service = AiCommitAllNotificationService(project, router)

        val notification = service.notifyError(
            title = "Error title",
            content = "Error content",
        )

        assertEquals(
            NotificationRequest(
                project = project,
                groupId = AiCommitAllNotificationService.GROUP_ID,
                title = "Error title",
                content = "Error content",
                type = NotificationType.ERROR,
            ),
            router.requests.single(),
        )
        assertSame(router.notifications.single(), notification)
    }

    @Test
    fun `plugin registers notification group used by service`() {
        val notificationGroup = pluginNotificationGroup()

        assertEquals(AiCommitAllNotificationService.GROUP_ID, notificationGroup.getAttribute("id"))
        assertEquals("BALLOON", notificationGroup.getAttribute("displayType"))
        assertEquals("true", notificationGroup.getAttribute("isLogByDefault"))
    }

    private class CapturingNotificationRouter : AiCommitAllNotificationRouter {
        val requests = mutableListOf<NotificationRequest>()
        val notifications = mutableListOf<Notification>()

        override fun notify(
            project: Project,
            groupId: String,
            title: String,
            content: String,
            type: NotificationType,
        ): Notification {
            val notification = Notification(groupId, title, content, type)
            requests += NotificationRequest(
                project = project,
                groupId = groupId,
                title = title,
                content = content,
                type = type,
            )
            notifications += notification
            return notification
        }
    }

    private data class NotificationRequest(
        val project: Project,
        val groupId: String,
        val title: String,
        val content: String,
        val type: NotificationType,
    )

    private fun pluginNotificationGroup(): Element {
        val groups = pluginDocument().getElementsByTagName("notificationGroup")
        return (0 until groups.length)
            .map { index -> groups.item(index) as Element }
            .single { group ->
                group.getAttribute("id") == AiCommitAllNotificationService.GROUP_ID
            }
    }

    private fun pluginDocument() = DocumentBuilderFactory.newInstance()
        .newDocumentBuilder()
        .parse(pluginXml.inputStream())

    private fun testProject(): Project = Proxy.newProxyInstance(
        Project::class.java.classLoader,
        arrayOf(Project::class.java),
    ) { proxy, method, args ->
        when (method.name) {
            "toString" -> "Test Project"
            "hashCode" -> System.identityHashCode(proxy)
            "equals" -> proxy === args?.firstOrNull()
            else -> method.defaultReturnValue()
        }
    } as Project

    private fun java.lang.reflect.Method.defaultReturnValue(): Any? = when (returnType) {
        java.lang.Boolean.TYPE -> false
        java.lang.Integer.TYPE -> 0
        java.lang.Long.TYPE -> 0L
        java.lang.Float.TYPE -> 0f
        java.lang.Double.TYPE -> 0.0
        java.lang.Void.TYPE -> null
        else -> null
    }

    private val pluginXml: Path =
        Path.of("src", "main", "resources", "META-INF", "plugin.xml")
}
